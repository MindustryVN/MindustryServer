package mindustry.server;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.backend.headless.HeadlessApplication;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.core.Logic;
import mindustry.core.NetServer;
import mindustry.core.Platform;
import mindustry.core.UI;
import mindustry.ctype.Content;
import mindustry.game.EventType.ServerLoadEvent;
import mindustry.mod.Mod;
import mindustry.mod.Mods.LoadedMod;
import mindustry.net.CrashSender;
import mindustry.net.Net;
import mindustry.ui.Fonts;

import static arc.util.Log.err;
import static mindustry.Vars.*;

public class ServerLauncher implements ApplicationListener {
    static String[] args;

    public static void main(String[] args) {
        try {
            ServerLauncher.args = args;
            Vars.platform = new Platform() {
            };
            Vars.net = new Net(platform.getNet());

            new HeadlessApplication(new ServerLauncher(), throwable -> CrashSender.send(throwable, f -> {
            }));
        } catch (Throwable t) {
            CrashSender.send(t, f -> {
            });
        }
    }

    @Override
    public void init() {
        Core.settings.setDataDirectory(Core.files.local("config"));
        loadLocales = false;
        headless = true;

        Vars.loadSettings();
        Vars.init();

        UI.loadColors();
        Fonts.loadContentIconsHeadless();

        content.createBaseContent();
        mods.loadScripts();
        content.createModContent();
        content.init();

        if (mods.hasContentErrors()) {
            err("Error occurred loading mod content:");
            for (LoadedMod mod : mods.list()) {
                if (mod.hasContentErrors()) {
                    err("| &ly[@]", mod.name);
                    for (Content cont : mod.erroredContent) {
                        err("| | &y@: &c@", cont.minfo.sourceFile.name(),
                                Strings.getSimpleMessage(cont.minfo.baseError).replace("\n", " "));
                    }
                }
            }
            err("The server will now exit.");
            System.exit(1);
        }

        bases.load();

        Core.app.addListener(new ApplicationListener() {
            public void update() {
                asyncCore.begin();
            }
        });
        Core.app.addListener(logic = new Logic());
        Core.app.addListener(netServer = new NetServer());
        Core.app.addListener(new ApplicationListener() {
            public void update() {
                asyncCore.end();
            }
        });

        mods.eachClass(mod -> {
            try {
                mod.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Events.fire(new ServerLoadEvent());
    }
}

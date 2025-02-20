package coloryr.allmusic.side.velocity;

import coloryr.allmusic.core.AllMusic;
import coloryr.allmusic.AllMusicVelocity;
import coloryr.allmusic.core.hud.HudUtils;
import coloryr.allmusic.core.music.play.PlayMusic;
import coloryr.allmusic.core.objs.config.SaveObj;
import coloryr.allmusic.core.objs.music.MusicObj;
import coloryr.allmusic.core.objs.music.SongInfoObj;
import coloryr.allmusic.core.side.ComType;
import coloryr.allmusic.core.side.ISide;
import coloryr.allmusic.core.sql.IEconomy;
import coloryr.allmusic.side.velocity.event.MusicAddEvent;
import coloryr.allmusic.side.velocity.event.MusicPlayEvent;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class SideVelocity extends ISide implements IEconomy {
    public static final Set<ServerConnection> TopServers = new CopyOnWriteArraySet<>();

    public static final Map<String, Integer> SendToBackend = new ConcurrentHashMap<>();

    public static void sendAllToServer(ServerConnection server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(0);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF(AllMusic.getMessage().PAPI.NoMusic);
        else {
            if (AllMusic.getConfig().MessageLimit
                    && PlayMusic.nowPlayMusic.getName().length() > AllMusic.getConfig().MessageLimitSize) {
                out.writeUTF(PlayMusic.nowPlayMusic.getName()
                        .substring(0, AllMusic.getConfig().MessageLimitSize));
            } else {
                out.writeUTF(PlayMusic.nowPlayMusic.getName());
            }
        }
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(1);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.nowPlayMusic.getAl());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(2);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.nowPlayMusic.getAlia());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(3);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.nowPlayMusic.getAuthor());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(4);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.nowPlayMusic.getCall());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(5);
        out.writeInt(PlayMusic.getSize());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(6);
        out.writeUTF(PlayMusic.getAllList());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());
    }

    public static void sendLyricToServer(ServerConnection server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(7);
        if (PlayMusic.lyric == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.lyric.getLyric());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(8);
        if (PlayMusic.lyric == null || PlayMusic.lyric.getTlyric() == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.lyric.getTlyric());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(9);
        out.writeBoolean(PlayMusic.lyric != null && PlayMusic.lyric.getTlyric() != null);
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(10);
        if (PlayMusic.lyric == null || PlayMusic.lyric.getKly() == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.lyric.getTlyric());
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(11);
        out.writeBoolean(PlayMusic.lyric != null && PlayMusic.lyric.getKly() != null);
        server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());
    }

    @Override
    public void send(String data, String player) {
        if (AllMusicVelocity.plugin.server.getPlayer(player).isPresent()) {
            send(AllMusicVelocity.plugin.server.getPlayer(player).get(), data);
        }
    }

    @Override
    public int getAllPlayer() {
        return AllMusicVelocity.plugin.server.getPlayerCount();
    }

    @Override
    public void bq(String data) {
        if (AllMusic.getConfig().MessageLimit
                && data.length() > AllMusic.getConfig().MessageLimitSize) {
            data = data.substring(0, AllMusic.getConfig().MessageLimitSize - 1) + "...";
        }
        Component message = Component.text(data);
        for (RegisteredServer server : AllMusicVelocity.plugin.server.getAllServers()) {
            if (AllMusic.getConfig().NoMusicServer.contains(server.getServerInfo().getName()))
                continue;
            for (Player player : server.getPlayersConnected())
                if (!AllMusic.getConfig().NoMusicPlayer.contains(player.getUsername()))
                    player.sendMessage(message);
        }
    }

    @Override
    public void bqt(String data) {
        this.bq(data);
    }

    @Override
    public boolean needPlay() {
        int online = 0;
        for (RegisteredServer server : AllMusicVelocity.plugin.server.getAllServers()) {
            if (AllMusic.getConfig().NoMusicServer.contains(server.getServerInfo().getName()))
                continue;
            for (Player player : server.getPlayersConnected())
                if (!AllMusic.getConfig().NoMusicPlayer.contains(player.getUsername()))
                    online++;
        }
        return online > 0;
    }

    @Override
    protected void topSendStop() {
        try {
            for (Player player : AllMusicVelocity.plugin.server.getAllPlayers()) {
                send(player, ComType.stop);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void topSendStop(String name) {
        try {
            Optional<Player> player = AllMusicVelocity.plugin.server.getPlayer(name);
            if (!player.isPresent())
                return;
            send(player.get(), ComType.stop);
            AllMusic.removeNowPlayPlayer(player.get().getUsername());
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendMusic(String url) {
        try {
            for (Player player : AllMusicVelocity.plugin.server.getAllPlayers()) {
                String server = player.getCurrentServer().isPresent() ?
                        player.getCurrentServer().get().getServerInfo().getName() : null;
                if (AllMusic.isOK(player.getUsername(), server, false))
                    continue;
                send(player, ComType.play + url);
                AllMusic.addNowPlayPlayer(player.getUsername());
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void topSendMusic(String player, String url) {
        try {
            if (AllMusicVelocity.plugin.server.getPlayer(player).isPresent()) {
                Player player1 = AllMusicVelocity.plugin.server.getPlayer(player).get();
                String server = player1.getCurrentServer().isPresent() ?
                        player1.getCurrentServer().get().getServerInfo().getName() : null;
                if (AllMusic.isOK(player1.getUsername(), server, false))
                    return;
                send(ComType.play + url, player);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPic(String url) {
        try {
            for (Player player : AllMusicVelocity.plugin.server.getAllPlayers()) {
                if (ok(player))
                    continue;
                String name = player.getUsername();
                SaveObj obj = HudUtils.get(name);
                if (!obj.EnablePic)
                    continue;
                send(player, ComType.img + url);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c图片指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPic(String player, String url) {
        try {
            if (AllMusicVelocity.plugin.server.getPlayer(player).isPresent()) {
                Player player1 = AllMusicVelocity.plugin.server.getPlayer(player).get();
                if (ok(player1))
                    return;
                send(ComType.img + url, player);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c图片指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPos(String player, int pos) {
        try {
            if (AllMusicVelocity.plugin.server.getPlayer(player).isPresent()) {
                Player player1 = AllMusicVelocity.plugin.server.getPlayer(player).get();
                if (ok(player1))
                    return;
                send(ComType.pos + pos, player);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲位置指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudLyric(String data) {
        try {
            for (Player player : AllMusicVelocity.plugin.server.getAllPlayers()) {
                if (ok(player))
                    continue;
                SaveObj obj = HudUtils.get(player.getUsername());
                if (!obj.EnableLyric)
                    continue;
                send(player, ComType.lyric + data);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudInfo(String data) {
        try {
            for (Player player : AllMusicVelocity.plugin.server.getAllPlayers()) {
                if (ok(player))
                    continue;
                SaveObj obj = HudUtils.get(player.getUsername());
                if (!obj.EnableInfo)
                    continue;
                send(player, ComType.info + data);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词信息发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudList(String data) {
        try {
            for (Player player : AllMusicVelocity.plugin.server.getAllPlayers()) {
                if (ok(player))
                    continue;
                String name = player.getUsername();
                SaveObj obj = HudUtils.get(name);
                if (!obj.EnableList)
                    continue;
                send(player, ComType.list + data);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲列表发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudUtilsAll() {
        for (Player players : AllMusicVelocity.plugin.server.getAllPlayers()) {
            String Name = players.getUsername();
            try {
                SaveObj obj = HudUtils.get(Name);
                String data = new Gson().toJson(obj);
                send(data, Name);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void sendBar(String data) {
        Component message = Component.text(data);
        for (Player player : AllMusicVelocity.plugin.server.getAllPlayers()) {
            try {
                if (ok(player))
                    continue;
                player.sendActionBar(message);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void clearHud(String player) {
        try {
            send(ComType.clear, player);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void clearHud() {
        try {
            Collection<Player> values = AllMusicVelocity.plugin.server.getAllPlayers();
            for (Player players : values) {
                send(players, ComType.clear);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessaget(Object obj, String message) {
        CommandSource sender = (CommandSource) obj;
        sender.sendMessage(Component.text(message));
    }

    @Override
    public void sendMessage(Object obj, String message) {
        CommandSource sender = (CommandSource) obj;
        sender.sendMessage(Component.text(message));
    }

    @Override
    public void sendMessageRun(Object obj, String message, String end, String command) {
        CommandSource sender = (CommandSource) obj;
        TextComponent endtext = Component.text(end)
                .clickEvent(ClickEvent.runCommand(command));
        TextComponent send = Component.text(message).append(endtext);
        sender.sendMessage(send);
    }

    @Override
    public void sendMessageSuggest(Object obj, String message, String end, String command) {
        CommandSource sender = (CommandSource) obj;
        TextComponent endtext = Component.text(end)
                .clickEvent(ClickEvent.suggestCommand(command));
        TextComponent send = Component.text(message).append(endtext);

        sender.sendMessage(send);
    }

    @Override
    public void runTask(Runnable run) {
        AllMusicVelocity.plugin.server.getScheduler().buildTask(AllMusicVelocity.plugin, run).schedule();
    }

    @Override
    public void reload() {
        new AllMusic().init(AllMusicVelocity.plugin.dataDirectory.toFile());
    }

    @Override
    public boolean checkPermission(String player, String permission) {
        try {
            if (AllMusic.getConfig().Admin.contains(player))
                return false;
            Player player1 = AllMusicVelocity.plugin.server.getPlayer(player).get();
            player1.hasPermission(permission);
        } catch (NoSuchElementException ignored) {

        }
        return true;
    }

    @Override
    public void runTask(Runnable run, int delay) {
        AllMusicVelocity.plugin.server.getScheduler().buildTask(AllMusicVelocity.plugin, run)
                .delay(delay, TimeUnit.MICROSECONDS).schedule();
    }

    @Override
    public void ping() {
        Iterator<ServerConnection> iterator = TopServers.iterator();
        while (iterator.hasNext()) {
            ServerConnection server = iterator.next();
            try {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeInt(200);
                server.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());
            } catch (Exception e) {
                iterator.remove();
            }
        }
    }

    @Override
    public boolean onMusicPlay(SongInfoObj obj) {
        MusicPlayEvent event = new MusicPlayEvent(obj);
        AllMusicVelocity.plugin.server.getEventManager().fire(event).join();
        return event.isCancel();
    }

    @Override
    public boolean onMusicAdd(Object obj, MusicObj music) {
        MusicAddEvent event = new MusicAddEvent(music, (CommandSource) obj);
        AllMusicVelocity.plugin.server.getEventManager().fire(event).join();
        return event.isCancel();
    }

    @Override
    public void updateInfo() {
        for (ServerConnection server : TopServers) {
            try {
                sendAllToServer(server);
            } catch (Exception e) {
                TopServers.remove(server);
            }
        }
    }

    @Override
    public void updateLyric() {
        for (ServerConnection server : TopServers) {
            try {
                sendLyricToServer(server);
            } catch (Exception e) {
                TopServers.remove(server);
            }
        }
    }

    private void send(Player players, String data) {
        if (players == null)
            return;
        try {
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            ByteBuf buf = Unpooled.buffer(bytes.length + 1);
            buf.writeByte(666);
            buf.writeBytes(bytes);
            runTask(() -> players.sendPluginMessage(AllMusicVelocity.channel, buf.array()));
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
            e.printStackTrace();
        }
    }

    private boolean ok(Player player) {
        String server = player.getCurrentServer().isPresent() ?
                player.getCurrentServer().get().getServerInfo().getName() : null;
        return AllMusic.isOK(player.getUsername(), server, true);
    }

    @Override
    public boolean check(String name, int cost) {
        return topEconomy(name, cost, 12);
    }

    @Override
    public boolean cost(String name, int cost) {
        return topEconomy(name, cost, 13);
    }

    private boolean topEconomy(String name, int cost, int type) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(type);
        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (SendToBackend.containsKey(uuid));

        SendToBackend.put(uuid, -1);
        String server = AllMusic.getConfig().Economy.Backend;
        ServerConnection toServer = null;
        for (ServerConnection connection : TopServers) {
            if (connection.getServerInfo().getName().equalsIgnoreCase(server)) {
                toServer = connection;
            }
        }
        if (toServer == null) {
            AllMusic.log.warning("§d[AllMusic]§c没有找到目标服务器");
            return false;
        }

        out.writeUTF(uuid);
        out.write(cost);
        out.writeUTF(name);

        toServer.sendPluginMessage(AllMusicVelocity.channelBC, out.toByteArray());

        Integer res;

        int count = 0;

        do {
            try {
                res = SendToBackend.get(uuid);
                if (res == null)
                    return false;
                else if (res == -1) {
                    Thread.sleep(1);
                    count++;
                } else if (res == 0) {
                    AllMusic.log.warning("§d[AllMusic]§c后端经济插件错误");
                    SendToBackend.remove(uuid);
                    return false;
                } else if (res == 1) {
                    SendToBackend.remove(uuid);
                    return false;
                } else if (res == 2) {
                    SendToBackend.remove(uuid);
                    return true;
                }
            } catch (Exception e) {
                AllMusic.log.warning("§d[AllMusic]§c经济数据发送错误");
                e.printStackTrace();
            }
        } while (count < 100);

        AllMusic.log.warning("§d[AllMusic]§c经济数据请求超时");

        return false;
    }
}

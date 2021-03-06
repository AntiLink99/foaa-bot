package bot.api;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import bot.dto.Playlist;
import bot.dto.Song;
import bot.dto.SongDifficulties;
import bot.listeners.PlaylistDifficultyListener;
import bot.main.BotConstants;
import bot.utils.Messages;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class BeatSaver {

	HttpMethods http;
	Gson gson;

	public BeatSaver() {
		http = new HttpMethods();
		gson = new Gson();
	}

	public void sendPlaylistInChannelByKeys(List<String> mapKeys, String playlistTitle, String playlistImageBase64, MessageChannel channel) {
		Playlist playlist;
		try {
			playlist = buildPlaylistByKeys(mapKeys, playlistImageBase64, playlistTitle);
		} catch (IllegalArgumentException e) {
			Messages.sendTempMessage(e.getMessage(), 10, channel);
			return;
		}
		createAndSendPlaylistFile(playlist, channel);
	}
	
	public void sendPlaylistInChannelBySongs(List<Song> songs, String playlistTitle, String playlistImageBase64, MessageChannel channel) {
		Playlist playlist;
		try {
			playlist = buildPlaylist(songs, playlistImageBase64, playlistTitle);
		} catch (IllegalArgumentException e) {
			Messages.sendTempMessage(e.getMessage(), 10, channel);
			return;
		}
		createAndSendPlaylistFile(playlist, channel);
	}

	public void sendRecruitingPlaylistInChannel(List<String> mapKeys, String playlistTitle, String playlistImageBase64, MessageReceivedEvent event) {
		TextChannel channel = event.getTextChannel();
		Playlist playlist;
		try {
			playlist = buildPlaylistByKeys(mapKeys, playlistImageBase64, playlistTitle);
		} catch (IllegalArgumentException e) {
			Messages.sendTempMessage(e.getMessage(), 10, channel);
			return;
		}

		askForPlaylistDifficultiesAndSendEmbed(playlist, event);
	}

	private void askForPlaylistDifficultiesAndSendEmbed(Playlist playlist, MessageReceivedEvent event) {
		event.getJDA().addEventListener(new PlaylistDifficultyListener(playlist, event, this));
	}

	private Playlist buildPlaylistByKeys(List<String> mapKeys, String image, String playlistTitle) {
		LinkedList<Song> songs = mapKeys.stream().map(key -> fetchSongByKey(key)).collect(Collectors.toCollection(LinkedList::new));
		return buildPlaylist(songs, image, playlistTitle);
	}
	
	private Playlist buildPlaylist(List<Song> songs, String image, String playlistTitle) {
		if (songs == null || songs.contains(null)) {
			throw new IllegalArgumentException("At least one the given keys is invalid.");
		} else if (songs.size() == 0) {
			throw new IllegalArgumentException("Please enter at least one key after the title.");
		}

		Playlist playlist = new Playlist();
		playlist.setPlaylistAuthor(BotConstants.playlistAuthor);
		playlist.setImage(image);
		playlist.setPlaylistTitle(playlistTitle);
		playlist.setSongs(songs);
		return playlist;
	}

	public void createAndSendPlaylistFile(Playlist playlist, MessageChannel channel) {
		String playlistJson = gson.toJson(playlist);
		File playlistFile = new File("src/main/resources/" + playlist.getPlaylistTitle().toLowerCase() + ".bplist");
		try {
			FileUtils.writeStringToFile(playlistFile, playlistJson, "UTF-8");
			if (playlistFile.exists()) {
				Messages.sendFile(playlistFile, playlistFile.getName(), channel);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		playlistFile.delete();
	}

	private Song fetchSongByKey(String key) {
		String infoUrl = ApiConstants.BS_MAP_DETAILS_URL + key;
		JsonObject response = http.fetchJsonObject(infoUrl);
		return getSongFromJson(response);
	}

	public Song fetchSongByHash(String hash) {
		String infoUrl = ApiConstants.BS_MAP_BY_HASH_URL + hash;
		JsonObject response = http.fetchJsonObject(infoUrl);
		return getSongFromJson(response);
	}

	private Song getSongFromJson(JsonObject json) {
		try {
			String hash = json.get("hash").getAsString();
			String songName = json.get("metadata").getAsJsonObject().get("songName").getAsString();
			String songKey = json.get("key").getAsString();
			String coverURL = ApiConstants.BS_PRE_URL + json.get("coverURL").getAsString();
			JsonObject diffJson = json.get("metadata").getAsJsonObject().get("difficulties").getAsJsonObject();

			SongDifficulties difficulties = new SongDifficulties();
			difficulties.setEasy(diffJson.get("easy").getAsBoolean());
			difficulties.setNormal(diffJson.get("normal").getAsBoolean());
			difficulties.setHard(diffJson.get("hard").getAsBoolean());
			difficulties.setExpert(diffJson.get("expert").getAsBoolean());
			difficulties.setExpertPlus(diffJson.get("expertPlus").getAsBoolean());

			Song song = new Song(hash,songName);
			song.setSongKey(songKey);
			song.setDifficulties(difficulties);
			song.setCoverURL(coverURL);
			return song;
		} catch (NullPointerException e) {
			return null;
		}
	}
}

package com.github.niwaniwa.we.core.twitter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.niwaniwa.we.core.WhiteEggCore;
import com.github.niwaniwa.we.core.event.WhiteEggTweetEvent;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetTask extends BukkitRunnable {

	private static final Pattern urlPattern =  Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+",
			Pattern.CASE_INSENSITIVE);
	private static final File path = new File(WhiteEggCore.getInstance().getDataFolder() + "/temp/");

	private TwitterManager twitter;
	private String tweet;
	private List<String> url = new ArrayList<>();;
	private boolean useMedia;
	private List<Status> status;
	private boolean successfull = false;

	public TweetTask(TwitterManager twitter, String tweet){
		this.twitter = twitter;
		checkURL(tweet);
		useMedia = !url.isEmpty();
		status = new ArrayList<>();
	}

	public void checkURL(String tweet){
		final Matcher matcher = urlPattern
				.matcher(tweet);
		String toTweet = tweet;
		while (matcher.find()) {
			String url = matcher.group();
			if(checkExtension(url)){
				toTweet = toTweet.replace(url, "");
				this.url.add(url);
			}
		}
		this.tweet = toTweet;
	}

	@Override
	public void run() {
		try {
			tweet();
		} catch (TwitterException e) {
		}
	}

	public void tweet() throws TwitterException{
		Twitter t = twitter.getTwitter();
		if(twitter instanceof PlayerTwitterManager){
			PlayerTwitterManager pt = (PlayerTwitterManager) twitter;
			WhiteEggTweetEvent event = new WhiteEggTweetEvent(pt.getPlayer(), tweet);
			if(event.isCancelled()){
				return;
			}
			tweet = event.getTweet();
		}
		Status s = t.updateStatus(build());
		if(s != null){
			this.successfull = true;
			status.add(s);
			return;
		}
		this.successfull = false;
	}

	private StatusUpdate build(){
		StatusUpdate su = new StatusUpdate(tweet);
		if(!useMedia){ return su; }
		for(String url : this.url){
			File media = readImage(url);
			if(media != null){
				su = su.media(media);
			}
		}
		return su;
	}

	public boolean isSuccessfull() {
		return successfull;
	}

	public List<Status> getStatus() {
		return status;
	}

	public void setStatus(List<Status> status) {
		this.status = status;
	}

	public boolean isMedia() {
		return useMedia;
	}

	public void setMedia(boolean media) {
		this.useMedia = media;
	}

	public List<String> getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url.add(url);
	}

	public String getTweet() {
		return tweet;
	}

	public File readImage(String url){
		if(!path.exists()){ path.mkdirs(); }
		InputStream input = responce(url);
		if(input == null){ return null; }
		String extension = url.split("\\.")[url.split("\\.").length - 1];
		File imagePath = new File(path, UUID.randomUUID() + "." + extension);
		BufferedImage image = null;
		try {
			image = ImageIO.read(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(image == null){ return null; }
		try {
			ImageIO.write(image, extension, imagePath);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return imagePath;
	}

	public InputStream responce(String url){
		if(!checkExtension(url)){ return null; }
		HttpClient client = create();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = null;
		try {
			response = client.execute(httpGet);
		} catch (IOException e){
			close(response);
			return null;
		}
		if(response.getStatusLine().getStatusCode() == 404){ return null; }
		InputStream input = null;
		try {
			input = response.getEntity().getContent();
		} catch (IllegalStateException | IOException e) {}
		return input;
	}

	private void close(HttpResponse responce){
		if(responce == null){ return; }
		HttpClientUtils.closeQuietly(responce);
	}

	private HttpClient create(){
		RequestConfig request = RequestConfig.DEFAULT;
		HttpClient http = HttpClientBuilder.create().setDefaultRequestConfig(request).build();
		return http;
	}

	private boolean checkExtension(String url){
		String[] format = new String[]{".jpg", ".gif", ".png"};
		for(String s : format){
			if(url.endsWith(s)){ return true; }
			continue;
		}
		return false;
	}

}

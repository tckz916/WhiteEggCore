package com.github.niwaniwa.we.core.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonManager {


	public JsonManager() {
	}

	/**
	 * localにファイルを保存します
	 * @param path 保存フォルダ
	 * @param file ファイル名
	 * @param json json
	 * @param chara 文字コード
	 * @return 成功したか
	 * @throws IOException 入出力
	 */
	public boolean writeJSON(File path, String file, JSONObject json, String chara) throws IOException {
		if(!path.exists()){ path.mkdirs(); }
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path, file)), chara));
		PrintWriter pw = new PrintWriter(bw);
		pw.write(json.toString());
		pw.close();
		return true;
	}

	public boolean writeJSON(File path, String file, JSONObject json) throws IOException {
		return writeJSON(path, file, json, "UTF-8");
	}

	public boolean writeJSON(File path, String file, JSONObject json, boolean backup) throws IOException {
		if(backup){
			File f = new File(path, file);
			if(f.exists()){
				f.renameTo(new File(path, file + "_old"));
			}
		}
		writeJSON(path, file, json);
		return true;
	}

	public boolean writeJSON(String path, String file, JSONObject json) throws IOException {
		return writeJSON(new File(file), file, json);
	}

	/**
	 * local
	 * @param file パス
	 * @return JSONObject json
	 * @throws IOException 入出力
	 */
	public JSONObject getJSON(File file) throws IOException {
		if (!file.exists()) { return null; }
		if (file.isDirectory()) { return null; }
		if (!file.getName().endsWith(".json")) { return null; }
		BufferedReader reader = new BufferedReader(new FileReader(file));
		StringBuilder b = new StringBuilder();
		String str = reader.readLine();
		while (str != null) {
			b.append(str);
			str = reader.readLine();
		}
		reader.close();
		if (b.length() == 0) { return null; }
		return JSONObject.fromObject(b.toString());
	}

	public  static Map<String, Object> toMap(Object o){
		Map<String, Object> map = new HashMap<>();
		if(!(o instanceof JSONObject)){
			if(!(o instanceof JSONArray)){ return map; }
		}
		JSONObject json = (JSONObject) o;
		for(Object key : json.keySet()){
			map.put(String.valueOf(key), json.get(key));
		}
		return map;
	}

	public static List<Object> toList(JSONArray array){
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.size(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

}

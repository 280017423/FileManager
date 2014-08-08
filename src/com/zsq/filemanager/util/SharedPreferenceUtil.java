package com.zsq.filemanager.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 
 * SharedPreference操作类； 在操作实体类的时候做为SharePreference的实体Model，必须带无参构造方法(默认即可)；<br>
 * Model注意地方：<br>
 * 1、数据类型只能为String,Integer等基本对象类型，不能为int,boolean基本类型；<br>
 * 2、数据如果为fianl,private修饰是不会保存到SharePreference；<br>
 * 3、尽量对应的Model文件名保存为:对应Model中final变量SHAREPREFERENCES_NAME中；
 * 使用SharePreference即可以整个Model操作，也可以根据单个key操作；
 * 
 * @author zou.sq
 * @date 2013-7-9 下午3:28:55
 * @version 2013-7-9 下午3:28:55
 */
public class SharedPreferenceUtil {

	private static final String TAG = "SharedPreferenceUtil";
	private static String separator = "_";
	// 无效标记
	public static final int INVALID_CODE = -1;

	/**
	 * 根据KEY 查询字符串值
	 * 
	 * @param mContext
	 * @param key
	 * @return String
	 */
	public static String getStringValueByKey(Context mContext, String fileName, String key) {
		String value = "";
		if (!StringUtil.isNullOrEmpty(key)) {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
			value = sharedPreferences.getString(key, "");
		}
		return value;
	}

	/**
	 * 根据KEY 查询Boolean值
	 * 
	 * @param mContext
	 * @param key
	 * @return boolean
	 */
	public static boolean getBooleanValueByKey(Context mContext, String fileName, String key) {
		boolean value = false;
		if (!StringUtil.isNullOrEmpty(key)) {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
			value = sharedPreferences.getBoolean(key, false);
		}
		return value;
	}

	/**
	 * 根据KEY 查询Integer值
	 * 
	 * @param mContext
	 * @param key
	 * @return int
	 */
	public static int getIntegerValueByKey(Context mContext, String fileName, String key) {
		int value = INVALID_CODE;
		if (!StringUtil.isNullOrEmpty(key)) {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
			value = sharedPreferences.getInt(key, INVALID_CODE);
		}
		return value;
	}

	/**
	 * 根据KEY 查询float值
	 * 
	 * @param mContext
	 * @param key
	 * @return int
	 */
	public static float getFloatValueByKey(Context mContext, String fileName, String key) {
		float value = INVALID_CODE;
		if (!StringUtil.isNullOrEmpty(key)) {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
			value = sharedPreferences.getFloat(key, INVALID_CODE);
		}
		return value;
	}

	/**
	 * 避免多个地方同时修改所以加synchronized; 保存本地数据，在读取的时候就不需要加了；
	 * 
	 * @param mContext
	 * @param key
	 * @param value
	 * @return void
	 */
	public static synchronized void saveValue(Context mContext, String fileName, String key, Object value) {
		if (!StringUtil.isNullOrEmpty(key)) {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			if (value != null) {
				if (value instanceof String) {
					editor.putString(key, String.valueOf(value));
				} else if (value instanceof Integer) {
					editor.putInt(key, (Integer) value);
				} else if (value instanceof Boolean) {
					editor.putBoolean(key, (Boolean) value);
				}
				editor.commit();
			}
		}
	}

	/**
	 * 避免多个地方同时修改所以加synchronized;
	 * 
	 * @Title: removeValue
	 * @Description: 移除保存的数据
	 * @param mContext
	 *            上下文对象
	 * @param key
	 *            待移除数据的key
	 * @return void 返回类型
	 */
	public static synchronized void removeValue(Context mContext, String fileName, String key) {
		if (!StringUtil.isNullOrEmpty(key)) {
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.remove(key);
			editor.commit();
		}
	}

	/**
	 * 直接将对象保存，字段类型只能为String,Integer,Long,Boolean,Float,Double类型，如果有其它类型，
	 * 那么需要另做对象保存；
	 * 
	 * @param mContext
	 * @param fileName
	 * @param clazz
	 * @return void
	 * @throws
	 */
	public static synchronized void saveObject(Context mContext, String fileName, Object obj) {
		saveObject(mContext, fileName, obj, null);
	}

	/**
	 * 如果同一个对象可以在SharePreference中保存多个实例，如WeiboInfoModel; 可以有Sina，QQ，RenRen
	 * 直接将对象保存，字段类型只能为String,Integer,Long,Boolean,Float,Double类型，如果有其它类型，
	 * 那么需要另做对象保存；
	 * 
	 * @param mContext
	 * @param fileName
	 * @param obj
	 * @param keyPrefixType
	 *            前缀类型；
	 * @return void
	 * @throws
	 */
	public static synchronized void saveObject(Context mContext, String fileName, Object obj, String keyPrefixType) {
		String prefix = "";
		if (keyPrefixType != null && keyPrefixType.length() > 0) {
			prefix = keyPrefixType + separator;
		}
		Class clazz = obj.getClass();
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		// 获取实体类的所有属性，返回Field数组
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				if (field.getName() == null) {
					continue;
				}
				if (field.get(obj) == null) {
					editor.remove(prefix + field.getName());
					continue;
				}
				// 如果类型是String// 如果类型是Integer// 如果类型是Boolean 是封装类
				if (field.getType().toString().equals("class java.lang.String")) {
					editor.putString(prefix + field.getName(), field.get(obj).toString());
				} else if (field.getType().toString().equals("class java.lang.Integer")) {
					editor.putInt(prefix + field.getName(), Integer.parseInt(field.get(obj).toString()));
				} else if (field.getType().toString().equals("class java.lang.Boolean")) {
					editor.putBoolean(prefix + field.getName(), Boolean.parseBoolean(field.get(obj).toString()));
				} else if (field.getType().toString().equals("class java.lang.Long")) {
					editor.putLong(prefix + field.getName(), Long.parseLong(field.get(obj).toString()));
				} else if (field.getType().toString().equals("class java.lang.Double")
						|| field.getType().toString().equals("class java.lang.Float")) {
					editor.putFloat(prefix + field.getName(), Float.parseFloat(field.get(obj).toString()));
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		editor.commit();
	}

	/**
	 * 直接从SharePreference中获得需要的实体类；
	 * 
	 * @Method: getObject
	 * @param mContext
	 * @param fileName
	 * @param clazz
	 *            需要返回的实体类变量；
	 * @param @return
	 * @return Object
	 * @throws
	 */
	public static Object getObject(Context mContext, String fileName, Class clazz) {
		return getObject(mContext, fileName, clazz, null);
	}

	/**
	 * 
	 * @Method: getObject
	 * @param mContext
	 * @param fileName
	 * @param clazz
	 * @param keyPrefixType
	 * @return Object
	 * @throws
	 */
	public static Object getObject(Context mContext, String fileName, Class clazz, String keyPrefixType) {
		Object object = null;
		try {
			// 得到前缀字符串；
			String prefix = "";
			if (keyPrefixType != null && keyPrefixType.length() > 0) {
				prefix = keyPrefixType + separator;
			}

			object = clazz.newInstance();
			SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
			// 获取实体类的所有属性，返回Field数组
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Class<?> typeClass = field.getType();
				Constructor<?> con;
				Object valueObj = null;
				// 如果类型是String// 如果类型是Integer// 如果类型是Boolean 是封装类
				if (typeClass.toString().equals("class java.lang.String")) {
					con = typeClass.getConstructor(String.class);
					String value = sharedPreferences.getString(prefix + field.getName(), "");
					valueObj = con.newInstance(value);
				} else if (typeClass.toString().equals("class java.lang.Integer")) {
					con = typeClass.getConstructor(String.class);
					Integer value = sharedPreferences.getInt(prefix + field.getName(), -1);
					valueObj = con.newInstance(value + "");
				} else if (typeClass.toString().equals("class java.lang.Boolean")) {
					con = typeClass.getConstructor(String.class);
					Boolean value = sharedPreferences.getBoolean(prefix + field.getName(), false);
					valueObj = con.newInstance(value + "");
				} else if (typeClass.toString().equals("class java.lang.Long")) {
					con = typeClass.getConstructor(String.class);
					Long value = sharedPreferences.getLong(prefix + field.getName(), -1);
					valueObj = con.newInstance(value + "");
				} else if (typeClass.toString().equals("class java.lang.Double")
						|| typeClass.toString().equals("class java.lang.Float")) {
					con = typeClass.getConstructor(String.class);
					Float value = sharedPreferences.getFloat(prefix + field.getName(), -1f);
					valueObj = con.newInstance(value + "");
				}
				if (valueObj != null) {
					if (Modifier.toString(field.getModifiers()) != null
							&& Modifier.toString(field.getModifiers()).endsWith("final")) {
						// 如果为final类型，那么不能赋值，不然会报错；
					} else {
						field.set(object, valueObj);
					}
				}
			}
			return object;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * 清除当前Model的本地存储；
	 * 
	 * @Method: removeObject
	 * @param mContext
	 * @param fileName
	 * @return void
	 * @throws
	 */
	public static void clearObject(Context mContext, String fileName) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}

	/**
	 * 清除当前Model的本地存储以某些关键字为前缀的缓存；如同时删除会员卡信息UserReq.clearMemberCardInfo()
	 * 
	 * @Method: removeObject
	 * @param mContext
	 * @param fileName
	 * @return void
	 * @throws
	 */
	public static void clearAllByPrefix(Context mContext, String fileName, String prefix) {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();

		Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();

		Set<Entry<String, String>> set = map.entrySet();
		for (Iterator<Map.Entry<String, String>> it = set.iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
			// 如果是以指定的prefix为前缀，则删除；
			if (entry.getKey().startsWith(prefix)) {
				editor.remove(entry.getKey());
			}
		}
		editor.commit();
	}
}

package com.wifiin.util.regex;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wifiin.exception.RegexException;

public class RegexUtil {
	/**类c语法的注释正则表达式*/
	private static Pattern COMMENT;
	private static Pattern DIGIT;
	private static Pattern INTEGER;
	private static Pattern BASE64;
	private static Pattern blankCharRegex;
	private static Pattern IP4;
	private static Pattern MAC_REGEX;
	private static Cache<String,Pattern> patternCache=CacheBuilder.newBuilder().build();
	
	private static Pattern getBase64(){
		if(BASE64==null){
			synchronized(RegexUtil.class){
				if(BASE64==null){
					BASE64=Pattern.compile("^[a-zA-Z0-9/+]{2,}={0,2}$");
				}
			}
		}
		return BASE64;
	}
	
	private static Pattern getCommentRegex(){
		if(COMMENT==null){
			synchronized(RegexUtil.class){
				if(COMMENT==null){
					COMMENT=Pattern.compile("//.*|(/\\*([^(\\*/)]*))+(\\*/{1}?)");
				}
			}
		}
		return COMMENT;
	}
	/**
	 * 删除文本中的所有类c语法注释内容
	 * */
	public static String removeComments(String code){
		return getCommentRegex().matcher(code).replaceAll("");
	}
	
	private static Pattern getBlankCharRegex(){
		if(blankCharRegex==null){
			synchronized(RegexUtil.class){
				if(blankCharRegex==null){
					blankCharRegex=Pattern.compile("\\s+");
				}
			}
		}
		return blankCharRegex;
	}
	/**
	 * 把txt中所有空白字符替换成target
	 * @param txt
	 * @param target
	 * @return String
	 * @see
	 */
	public static String replaceAllBlanks(String txt, String target){
		return getBlankCharRegex().matcher(txt).replaceAll(target);
	}
	
	private static Pattern getDigitRegex(){
		if(DIGIT==null){
			synchronized(RegexUtil.class){
				if(DIGIT==null){
					DIGIT=Pattern.compile("^(\\d*\\.)?\\d+|\\d+(\\.?\\d*)?$");
				}
			}
		}
		return DIGIT;
	}
	private static Pattern getIntegerRegex(){
		if(INTEGER==null){
			synchronized(RegexUtil.class){
				if(INTEGER==null){
					INTEGER=Pattern.compile("^\\d+$");
				}
			}
		}
		return INTEGER;
	}
	/**
	 * 判断src是否数字串
	 * @param src
	 * @return boolean
	 */
	public static boolean isDigit(String src){
		return getDigitRegex().matcher(src).matches();
	}
	/**
	 * 判断src是否整数串
	 * @param src
	 * @return
	 */
	public static boolean isInteger(String src){
		return getIntegerRegex().matcher(src).matches();
	}
	public static boolean isBase64(String src){
		return src.length()%4==0 && getBase64().matcher(src).matches();
	}
	
	private static Pattern getIp4Regex(){
		if(IP4==null){
			synchronized(Pattern.class){
				if(IP4==null){
					IP4=Pattern.compile("^(((00)?\\d|0?1?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}((00)?\\d|0?1?\\d\\d|2[0-4]\\d|25[0-5])$");
				}
			}
		}
		return IP4;
	}
	public static boolean isIp4(String src){
		return getIp4Regex().matcher(src).matches();
	}
	
	private static Pattern getMacRegex(){
		if(MAC_REGEX==null){
			synchronized(RegexUtil.class){
				if(MAC_REGEX==null){
					MAC_REGEX=Pattern.compile("^(([0-9a-z]{2}:)|([0-9a-z]{2}-)){5}[0-9a-z]{2}$");
				}
			}
		}
		return MAC_REGEX;
	}
	
	public static boolean isMac(String mac){
		return getMacRegex().matcher(mac.toLowerCase()).matches();
	}
	public static Pattern getRegex(String regex){
		try{
            return patternCache.get(regex,()->{
                return Pattern.compile(regex);
            });
        }catch(ExecutionException e){
            throw new RegexException(e);
        }
	}
	public static Pattern getRegex(String regex, int flags){
		return getRegex('/'+regex+'/'+flags);
	}
	
	public static String[] split(CharSequence src, String regex){
		return getRegex(regex).split(src);
	}
	public static boolean matches(CharSequence src, String regex){
		return getRegex(regex).matcher(src).matches();
	}
	public static boolean isHex(CharSequence src){
		return getRegex("^[a-fA-F0-9]+$").matcher(src).matches();
	}
	public static boolean isHex(CharSequence src,int length){
	    return getRegex("^[a-z0-9]{"+length+"}$").matcher(src.toString().toLowerCase()).matches();
	}
	/**
	 * 
	 * @param src
	 * @param min  最小长度
	 * @param max  最大长度，如果小于0，认为没有最大长度限制
	 * @return
	 */
	public static boolean isHex(CharSequence src, int min, int max){
		return getRegex("^[a-z0-9]{"+min+','+(max>=0?max:"")+"}$").matcher(src.toString().toLowerCase()).matches();
	}
	public static String replaceFirst(CharSequence src, String regex,String replacement){
		return getRegex(regex).matcher(src).replaceFirst(replacement);
	}
	public static String replaceAll(CharSequence src, String regex, String replacement){
		return getRegex(regex).matcher(src).replaceAll(replacement);
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
//		for(char start='\u4e00';start<='\u9fa5';start++){
//			String c=Character.toString(start);
//			int l=c.getBytes("UTF-8").length;
//			System.out.println(c+"	"+l+"	"+(l!=3?true:false));
//		}
	}
}

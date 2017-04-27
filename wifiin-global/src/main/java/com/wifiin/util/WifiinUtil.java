package com.wifiin.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.wifiin.common.CommonConstant;
import com.wifiin.common.GlobalObject;
import com.wifiin.constant.ClientType;
import com.wifiin.constant.WifiinConstant;
import com.wifiin.util.digest.MessageDigestUtil;
import com.wifiin.util.geo.GeoUtil;
import com.wifiin.util.regex.RegexUtil;
import com.wifiin.util.string.ThreadLocalStringBuilder;

/**
 * wifiin通用工具类
 * 
 * @author 吴京润
 *         
 */
public class WifiinUtil {
    private static final int     LOOKUPLENGTH         = 64;
    private static final char[]  lookUpBase64Alphabet = new char[LOOKUPLENGTH];
    private static Map<Character,Integer> lookUpBase64AlphabetMap;
    static {

        for (int i = 0; i <= 25; i++) {
            lookUpBase64Alphabet[i] = (char) ('A'+i);
        }

        for (int i = 26, j = 0; i <= 51; i++, j++) {
            lookUpBase64Alphabet[i] = (char) ('a'+j);
        }

        for (int i = 52, j = 0; i <= 61; i++, j++) {
            char c=(char)('0'+j);
            lookUpBase64Alphabet[i] = (char) ('0'+j);
        }
        lookUpBase64Alphabet[62] = (char) '+';
        lookUpBase64Alphabet[63] = (char) '/';
    }
    private static List<Character> getBase64List(){
        List<Character> list=new ArrayList<>();
        for(int i=0,l=lookUpBase64Alphabet.length;i<l;i++){
            list.add(lookUpBase64Alphabet[i]);
        }
        return Collections.unmodifiableList(list);
    }
    private static Map<Character,Integer> getBase64Map(){
        if(lookUpBase64AlphabetMap==null){
            synchronized(Base64.class){
                if(lookUpBase64AlphabetMap==null){
                    lookUpBase64AlphabetMap=new HashMap<Character,Integer>();
                    for(int i=0;i<64;i++){
                        lookUpBase64AlphabetMap.put(lookUpBase64Alphabet[i], i);
                    }
                    lookUpBase64AlphabetMap=Collections.unmodifiableMap(lookUpBase64AlphabetMap);
                }
            }
        }
        return lookUpBase64AlphabetMap;
    }
    /**
     * 根据指定用户id和当前时间毫秒数构造token
     * 
     * @param userid
     * @return
     */
    public static String createToken(String tokenTime, String sn, Object userid){
        return tokenTime + '-' + sn + '-' + userid;
    }
    
    public static String createToken(Date tokenTime, String sn, Object userid){
        return createToken(Help.dateToTxt(tokenTime, WifiinConstant.TOKEN_TIME_FORMAT), sn, userid);
    }
    
    public static String createTokenMd5(Date tokenTime, String sn, Object userid){
        return MessageDigestUtil.md5Hex(createToken(tokenTime, sn, userid), CommonConstant.DEFAULT_CHARSET_NAME);
    }
    
    public static boolean isTokenTimeout(Date tokenTime){
        return tokenTime.getTime() <= System.currentTimeMillis() - WifiinConstant.getTOKEN_LIFE() * 1000L;
    }
    
    /**
     * 接收点分隔的版本号，把点号去掉，把数字串转化成整数数组 把点分隔的版本号字符串转化成整数数组 如果版本号不合法，就返回null
     * 至少应有一个点号，头两个数字分别是主版本号和副版本号，返回的整数数组只包含主副版本号
     * 
     * @param cv
     * @return
     */
    public static int[] parseClientVersion(String cv){
        String[] vs = RegexUtil.split(cv, "\\.");
        if (vs.length >= 2) {// 版本号规则1
            int major = parseVersion(vs[0]), minor = parseVersion(vs[1]);
            if ((major >= 0 && minor > 0) || (major > 0 && minor >= 0)) {// 版本号规则2
                return vs.length == 2 ? new int[] { major, minor } : new int[] { major, minor, parseVersion(vs[2]) };
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    
    private static int parseVersion(String v){
        if (RegexUtil.isDigit(v)) {
            return Integer.parseInt(v);
        } else {
            return 0;
        }
    }
    
    /**
     * 接收省市两个字符串，把这两个字符串合并成一个，如果传入空值，就以空串代替
     * 
     * @param province
     * @param city
     * @return
     */
    public static String generateLocation(String province, String city){
        return Help.convert(province, "") + Help.convert(city, "");
    }
    
    
    /**
     * 计算指定等级需要的最低经验值
     * 
     * @author yujinhui modified by wujingrun
     * @param lv
     *            指定等级
     * @return 指定等级所需要的最低经验值
     */
    public static int calculateExpForLv(int lv){
        return 5 * lv * lv + 20 * lv;
    }
    
    /**
     * 计算指定等级的下一级所需要的最低经验值
     * 
     * @param lv
     *            指定等级
     * @return 指定等级的下一级所需要的最低经验值
     */
    public static int calculateExpForNextLv(int lv){
        return calculateExpForLv(lv + 1);
    }
    
    /**
     * 根据积分计算用户等级 最低一级，没有0级
     * 
     * @param points
     *            用户累积消耗的积分总数
     * @return
     */
    private static int calculateLevel(int growth){
        int lv = ((int) StrictMath.sqrt(400 + 20 * growth) - 20) / 10;
        return lv > 0 ? lv : 1;
    }
    
    public static int calculateLevel(int points, int exp){
        return calculateLevel(points + exp);
    }
    
    /**
     * 数据库表device的sn字段的取值 ios 安卓机采用mac，wp采用设备号 wp无法通过api取得mac
     * 曾经用imei标识安卓机，udid标识ios，但是imei经常取不到，苹果公司也要禁止取得udid，为了支持非越狱版就采用mac作为硬件标识
     * 
     * @param mac
     * @param deviceId
     * @return
     */
    public static String getSn(String mac, String deviceId){
        if (Help.isNotEmpty(mac) &&  Help.isEmpty(deviceId)) {
            return mac.toLowerCase();
        } else {
            return Help.convert(deviceId, "").toLowerCase();
        }
    }
    
    private static String getSn(String mac, String deviceId, String uuid){
        String sn = getSn(mac, deviceId);
        if (sn.equalsIgnoreCase(mac) || Help.isEmpty(uuid)) {
            return sn;
        } else {
            return sn + '_' + uuid;
        }
    }
    
    public static String getSn(String mac, String deviceId, int os, String osVersion, String openUdid, String uuid){
        if (uuid == null) {
            uuid = "";
        }
        if (isAfterIOS7(mac, os, osVersion)) {
            if (Help.isNotEmpty(mac) || Help.isEmpty(openUdid)) {
                return getSn(mac, deviceId, uuid);
            } else if (Help.isNotEmpty(uuid)) {
                return (openUdid + '_' + uuid).toLowerCase();
            } else {
                return openUdid.toLowerCase();
            }
        } else {
            return getSn(mac, deviceId, uuid);
        }
    }
    
    public static boolean isImei(String src){
        if (Help.isEmpty(src) || src.length() != 15) {
            return false;
        }
        char[] arr = src.toCharArray();
        int result = 0;
        int l = arr.length;
        for (int i = 0, ll = l - 1; i < ll; i++) {
            int c = arr[i] - '0';
            if (i % 2 == 0) {// 由于从0开始数，奇数位的下标索引实际是偶数
                result += c;
            } else {
                int multi = c * 2;
                int division = multi / 10;
                result += division + (multi - division * 10);
            }
        }
        result %= 10;
        return result == 0 || ((10 - result) == arr[l - 1] - '0');
    }
    
    public static boolean isValidMac(int os, String mac){
        return Help.isEmpty(mac) || WifiinUtil.isMac(mac);
    }
    
    public static boolean isMac(String src){
        return Help.isNotEmpty(src) && RegexUtil.matches(src, "^[a-fA-F0-9][02468aceACE][a-fA-F0-9]{10}$");
    }
    
    
    private static AtomicInteger SERIAL_NUM = new AtomicInteger(0);
    
    /**
     * 计算订单序列号，只有0-9十个数字
     * 
     * @see WifiinUtil.createOrderId(long, int)。只被这一个方法调用
     * @return
     */
    public static int getSerialNum(){
        int serial = SERIAL_NUM.getAndIncrement();
        if (serial > 9) {
            SERIAL_NUM.set(0);
            serial = SERIAL_NUM.getAndIncrement();
        }
        return serial;
    }
    
    /**
     * 计算订单号 第一位是用大写字母表示的年份，这个功能从2013年开始，2013年是A，依次类推
     * 第二位是16进制表示的月份，从1月到12月分别是0-B 接下来两位是创建订单时的日期 接下来五位是创建订单那一天的秒数 接下来是用户id
     * 
     * @param userId
     * @return
     */
    public static String createOrderId(long userId, int os, String businessId){
        return createOrderId(userId, os, businessId, null);
    }
    
    public static String createOrderId(long userId, int os, String businessId, Date time){
        Calendar now = Calendar.getInstance();
        if (time != null) {
            now.setTime(time);
        }
        String sec = Long.toString((now.getTimeInMillis() - Help.today().getTime()) / 1000);
        return Integer.toString(now.get(Calendar.YEAR), 36) + Integer.toHexString(now.get(Calendar.MONTH)).toUpperCase()
                + Integer.toString(now.get(Calendar.DATE), 36).toUpperCase() + Help.concat("0", 5 - sec.length()) + sec
                + userId + os + businessId + getSerialNum();
    }
    
    public static String createOrderId(long userId, int os, String businessId, long time){
        return createOrderId(userId, os, businessId, new Date(time));
    }
    
    public static String createOrderId(long userId, int os, String businessId, String time, String format)
            throws ParseException{
        return createOrderId(userId, os, businessId, Help.txtToDate(time, format));
    }
    
    /**
     * 根据orderId返回下单的操作系统编号
     */
    public static int parseOsFromOrderId(String orderId){
        return orderId.charAt(orderId.length() - 2) - '0';
    }
    
    /**
     * 从订单号计算出订单创建时间
     * 
     * @param orderId
     * @return
     */
    public static Date parseOrderMakeTime(String orderId){
        int startYear = 2013;
        Calendar makeDate = Calendar.getInstance();
        makeDate.set(Calendar.YEAR, orderId.charAt(0) - 'A' + startYear);
        makeDate.set(Calendar.MONTH, Integer.parseInt(orderId.substring(1, 2), 16));
        makeDate.set(Calendar.DATE, Integer.parseInt(orderId.substring(2, 3), 36));
        makeDate.set(Calendar.HOUR_OF_DAY, 0);
        makeDate.set(Calendar.MINUTE, 0);
        makeDate.set(Calendar.SECOND, 0);
        makeDate.set(Calendar.MILLISECOND, 0);
        return new Date(makeDate.getTimeInMillis() + Integer.parseInt(orderId.substring(3, 8)) * 1000);
    }
    
    /**
     * 把参数转化成字节数组并计算md5值，计算结果以base64形式返回
     * 
     * @param openid
     * @param loginType
     * @param userid
     * @param password
     * @return
     */
    public static String password2md5base64(String openid, int loginType, long userid, String password){
        byte[] openids = openid.getBytes();
        byte[] pass = password.getBytes();
        byte[] tomd = new byte[openids.length + pass.length + 1 + 8];
        System.arraycopy(openids, 0, tomd, 0, openids.length);
        System.arraycopy(pass, 0, tomd, openids.length, pass.length);
        int tomdl = tomd.length;
        for (int i = 1; i <= 8; i++) {
            tomd[tomdl - i] = (byte) (userid >> (8 * (i - 1)));
        }
        tomd[tomdl - 9] = (byte) loginType;
        return Base64.encodeBase64String(MessageDigestUtil.md5(tomd));
    }
    
    /**
     * 所有webservice接口的信息编号都以E或I开头，四位整数结束，把四位整数串转化成int，E开头的编号返回负数，I开头的返回正数
     * 
     * @param errCode
     * @return
     */
    public static int parseMsgCode(String errCode){
        if (errCode.startsWith("E")) {
            return -Integer.parseInt(errCode.substring(1));
        } else if (errCode.startsWith("I")) {
            return Integer.parseInt(errCode.substring(1));
        } else {
            throw new IllegalArgumentException("errCode must start with E or I, but it is " + errCode);
        }
    }
    
    private static final AtomicBoolean ON_SERVICE = new AtomicBoolean(true);
    
    /**
     * 停止服务，只在升级服务器时被调用
     * 
     * @see WifiinUtil.isOnService();
     */
    public static void stopService(){
        ON_SERVICE.set(!ON_SERVICE.get());
    }
    
    /**
     * 判断服务是否在运行，对数据案例要求较高的服务会在每次收到客户端访问时，调用这个方法判断服务是否可用
     * 
     * @see WifiinUtil.stopService();
     * @return
     */
    public static boolean isOnService(){
        return ON_SERVICE.get();
    }
    
    /**
     * 判断是不是ios7
     */
    public static boolean isAfterIOS7(String mac, int os, String osVersion){
        return ((os == ClientType.IOS.getType()
                || os == ClientType.IOS_SHOP.getType()) && Help.isNotEmpty(osVersion)
                && osVersion.startsWith("7"));
    }
    
    public static String generateFileName(String originFileName) throws UnsupportedEncodingException{
        char[] array = { 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l',
                'z', 'x', 'c', 'v', 'b', 'n', 'm', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'Q', 'W', 'E', 'R',
                'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N',
                'M', '-', '_' };
        java.math.BigInteger md5 = new java.math.BigInteger(originFileName.getBytes("utf8"));
        java.math.BigInteger radix = new java.math.BigInteger("63");// 64-1
        StringBuilder result = ThreadLocalStringBuilder.builder();
        while (!md5.equals(java.math.BigInteger.ZERO)) {
            java.math.BigInteger tmp = md5.divide(radix);
            result.append(array[md5.add(radix).intValue()]);// rest&63
            md5 = tmp;
        }
        return result.append(originFileName,originFileName.indexOf('.'),originFileName.length()).toString();
    }
    
    public static String randomApAccountPassword(String origine){
        if (Help.isEmpty(origine)) {
            return origine;
        }
        int l = origine.length();
        return RegexUtil.isInteger(origine) ? RandomStringUtils.random(l, "0123456789")
                : RandomStringUtils.random(l, "0123456789abcdefghijklmnopqrstuvwxyz");
    }
    
    public static Date getTomorrow(){
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        now.add(Calendar.DATE, 1);
        return now.getTime();
    }
    
    private static final int VERIFY_CODE_RANDOM_START = WifiinConstant.getVERIFY_CODE_RANDOM_START();
    
    /**
     * 为登录激活接口的verify字段创建值
     * 
     * @param device
     *            请求报文的deviceId字段，安卓是mac，ios7以下是udid,ios7以上是openUdid
     * @param time
     *            格式是yyyyMMddHHmmssSSS的当时间，取当前系统时间即可。服务器在验证生成的verify是否合法时，
     *            会认为在验证时刻的时间前后24小时的time都是合法时间
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String makeVerifyCode(String device, String time) throws UnsupportedEncodingException{
        List<Character> base64CList = getBase64List();
        String saltSrc = device + time;
        int saltIdx = saltSrc.length() / 2;
        int random = ThreadLocalRandom.current().nextInt(saltIdx);
        saltIdx = saltIdx + (random % 2 == 0 ? random : -random);
        String sub = saltSrc.substring(0, saltIdx);
//        int subLen = sub.length();
//        int timeLen = time.length();
        StringBuilder content = ThreadLocalStringBuilder.builder().append(saltSrc);
//        if (subLen > 0) {
//            content.append(sub.charAt((time.charAt(timeLen - 4) - '0') % subLen))
//                    .append(sub.charAt(subLen - 1 - (time.charAt(timeLen - 3) - '0') % subLen));
//        }
        verifyAppend(4,3,sub,time,content);
        sub = saltSrc.substring(saltIdx);
//        subLen = sub.length();
//        if (subLen > 0) {
//            content.append(sub.charAt((time.charAt(timeLen - 2) - '0') % subLen))
//                    .append(sub.charAt(subLen - 1 - (time.charAt(timeLen - 1) - '0') % subLen));
//        }
        verifyAppend(2,1,sub,time,content);
        char[] base64c = new char[base64CList.size()];
        for (int i = 0, l = base64c.length; i < l; i++) {
            base64c[i] = base64CList.get(i);
        }
        String verify=content.toString();
        return ThreadLocalStringBuilder.builder().append(
                Base64.encodeBase64String(MessageDigestUtil.md5Hex(verify, CommonConstant.DEFAULT_CHARSET_NAME).getBytes()))
                        .insert(VERIFY_CODE_RANDOM_START, RandomStringUtils.random(4, base64c))
                        .replace(VERIFY_CODE_RANDOM_START + 3, VERIFY_CODE_RANDOM_START + 4,
                                base64CList.get(saltIdx - 1).toString())
                        .toString();
    }
    
    /**
     * 验证激活接口的verify字段值
     * 
     * @param device
     *            请求报文的deviceId字段，安卓是mac，ios7以下是udid,ios7以上是openUdid
     * @param time
     *            格式是yyyyMMddHHmmssSSS的当时间，取当前系统时间即可。服务器在验证生成的verify是否合法时，
     *            会认为在验证时刻的时间前后24小时的time都是合法时间
     * @param code 待验证的值
     * @return
     * @throws UnsupportedEncodingException
     */
    public static boolean verify(String device, String time, String code) throws UnsupportedEncodingException{
        if (Help.isEmpty(time) || Help.isEmpty(code) || Help.isEmpty(device)) {
            return false;
        }
        Date input = null;
        try{
           input = Help.txtToDate(time, "yyyyMMddHHmmssSSS");
        }catch (ParseException e) {
            return false;
        }
        if (input == null) {
            return false;
        }
        int saltIdx = getBase64Map().get(code.charAt(VERIFY_CODE_RANDOM_START + 3)) + 1;
        String saltSrc = device + time;
        String sub = saltSrc.substring(0, saltIdx);
//        int subLen = sub.length();
//        int timeLen = time.length();
        StringBuilder content = ThreadLocalStringBuilder.builder().append(saltSrc);
//        if (subLen > 0) {
//            content.append(sub.charAt((time.charAt(timeLen - 4) - '0') % subLen))
//                   .append(sub.charAt(subLen - 1 - (time.charAt(timeLen - 3) - '0') % subLen));
//        }
        verifyAppend(4,3,sub,time,content);
        sub = saltSrc.substring(saltIdx);
//        subLen = sub.length();
//        if (subLen > 0) {
//            content.append(sub.charAt((time.charAt(timeLen - 2) - '0') % subLen))
//                   .append(sub.charAt(subLen - 1 - (time.charAt(timeLen - 1) - '0') % subLen));
//        }
        verifyAppend(2,1,sub,time,content);
        String verify=content.toString();
        return new String(Base64.decodeBase64(ThreadLocalStringBuilder.builder().append(code).delete(VERIFY_CODE_RANDOM_START, VERIFY_CODE_RANDOM_START + 4).toString()),CommonConstant.DEFAULT_CHARSET_NAME)
                     .equals(MessageDigestUtil.md5Hex(verify, CommonConstant.DEFAULT_CHARSET_NAME));
    }
    public static void verifyAppend(int timeIdx0,int timeIdx1,String sub,String time,StringBuilder content){
        int subLen=sub.length();
        int timeLen=time.length();
        if (subLen > 0) {
            content.append(sub.charAt((time.charAt(timeLen - timeIdx0) - '0') % subLen))
                   .append(sub.charAt(subLen - 1 - (time.charAt(timeLen - timeIdx1) - '0') % subLen));
        }
    }
    /**
     * 比较客户端当前版本和指定版本，如果低于指定版本返回-1，高于指定版本返回1，否则返回0
     * 
     * @param client
     *            当前客户端版本
     * @param compared
     *            指定比较的版本
     * @return
     */
    public static int compareClientVersion(String client, String compared){
        if (client.equals(compared)) {
            return 0;
        }
        return compareClientVersion(client, WifiinUtil.parseClientVersion(compared));
    }
    
    public static int compareClientVersion(String client, int... compared){
        return compareClientVersion(WifiinUtil.parseClientVersion(client), compared);
    }
    
    public static int compareClientVersion(int[] client, int... compared){
        int currentMajor = client[0], currentMinor = client[1], currentBuild = client.length<3?0:client[2];
        int specifiedMajor = compared[0], specifiedMinor = compared[1], specifiedBuild = compared.length<3?0:compared[2];
        if (currentMajor > specifiedMajor) {
            return 1;
        } else if (currentMajor == specifiedMajor) {
            if (currentMinor > specifiedMinor) {
                return 1;
            } else if (currentMinor == specifiedMinor) {
                if (currentBuild > specifiedBuild) {
                    return 1;
                } else if (currentBuild == specifiedBuild) {
                    return 0;
                }
            }
        }
        return -1;
    }
    
    public static boolean isClientVersionUpperThanSpecified(String client, String compared){
        return compareClientVersion(client, compared) > 0 ? true : false;
    }
    
    public static boolean isClientVersionUpperThanSpecified(String client, int... compared){
        return compareClientVersion(client, compared) > 0 ? true : false;
    }
    
    public static boolean isClientVersionUpperThanSpecified(int[] client, int... compared){
        return compareClientVersion(client, compared) > 0 ? true : false;
    }
    
    public static boolean isClientVersionLowerThanSpecified(String client, String compared){
        return compareClientVersion(client, compared) > 0 ? true : false;
    }
    
    public static boolean isClientVersionLowerThanSpecified(String client, int... compared){
        return compareClientVersion(client, compared) > 0 ? true : false;
    }
    
    public static boolean isClientVersionLowerThanSpecified(int[] client, int... compared){
        return compareClientVersion(client, compared) > 0 ? true : false;
    }
    
    /**
     * @param oriLatitude
     * @param oriLongitude
     * @param desLatitude
     * @param desLongitude
     * @return
     */
    public static int calculateDistance(BigDecimal oriLongitude, BigDecimal oriLatitude, BigDecimal desLongitude,
            BigDecimal desLatitude){
        return (int) Math.round(GeoUtil.calculateDistance(oriLongitude.doubleValue(), oriLatitude.doubleValue(),
                desLongitude.doubleValue(), desLatitude.doubleValue()));
    }
    
    public static String concatUrl(String domain,String uri){
        if (uri.startsWith("/")) {
            return domain + uri;
        }
        return domain + '/' + uri;
    }
    
    public static boolean hasChineseCharacter(String content){
        return Help.isNotEmpty(content) && RegexUtil.matches(content,".*[\u4e00-\u9fa5]+.*");
    }
    /**
     * 对params生成json，在后面添加signKey，最后计算sha1base64
     * @param params 计算签名的数据
     * @param signKey 签名KEY
     * @return sha1base64的结果
     * @throws JsonProcessingException
     */
    public static String makeSignAsJson(Map<String,Object> params,String signKey) throws JsonProcessingException{
        return makeSign(GlobalObject.getJsonMapper().writeValueAsString(params),signKey);
    }
    /**
     * 对paramsJson后面添加signKey最后计算sha1base64
     * @param src
     * @param signKey
     * @return
     */
    public static String makeSign(String src,String signKey){
        return MessageDigestUtil.sha1Base64(src+signKey);
    }
    @SuppressWarnings("rawtypes")
    private static String makeSignInParams(Map<String,Object> params,String signKey){
        List<String> signSrc = new ArrayList<>();// 声明一个list，保存所有请求参数值
        if (params.size() > 0 && params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Object o = entry.getValue();
                if (o instanceof Collection) {// 如果是集合或数组类型，把集合中的每个元素填充到list
                    for (Object c : (Collection) o) {
                        signSrc.add(c.toString());
                    }
                } else if (o.getClass().isArray()) {
                    for (int i = 0, l = Array.getLength(o); i < l; i++) {
                        signSrc.add(Array.get(o, i).toString());
                    }
                } else {
                    signSrc.add(o.toString());// 如果参数值不是集合或数组直接填充到list
                }
            }
            signSrc.add(signKey);
            Collections.sort(signSrc);// 把list按字符序排序
            StringBuilder signContent = ThreadLocalStringBuilder.builder();
            for (int i = 0, l = signSrc.size(); i < l; i++) {// 把所有请求参数构造成一个字符串
                signContent.append(signSrc.get(i));
            }
            String signStr=signContent.toString();
            String sign=MessageDigestUtil.sha1Base64(signStr);// 计算请求参数的SHA-1
            return sign;
        }
        return null;
    }
    public static boolean checkSignIngParams(Map<String, Object> params, String signKey) {
        return params.remove("sign").toString().equals(makeSignInParams(params,signKey));
    }
    public static boolean checkSignAsJson(Map<String, Object> params, String signKey,String sign) throws JsonProcessingException{
        return sign.equals(makeSignAsJson(params,signKey));
    }
    public static String shaSign(Map<String,Object> param,String partnerKey, String signKey){
        param.put("partnerKey", partnerKey);//partnerKey
        param.put("timestamp", Long.toString(System.currentTimeMillis()));//当前时间毫秒数
        param.put("nonce", RandomStringUtils.random(8,"0123456789"));//8位十进制随机字符串
        return makeSignInParams(param,signKey);
    }
    public static String shaSign(Map<String,Object> param,String partnerKey,String partnerUserId,String partnerArg, String signKey){
        param.put("partnerKey", partnerKey);//partnerKey
        param.put("partnerUserId",partnerUserId);
        param.put("partnerArg",partnerArg);
        param.put("timestamp", Long.toString(System.currentTimeMillis()));//当前时间毫秒数
        param.put("nonce", RandomStringUtils.random(8,"0123456789"));//8位十进制随机字符串
        return makeSignInParams(param,signKey);
    }
    public static String language(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String lang=(String)request.getAttribute("lang");
        if(Help.isEmpty(lang)){
            lang=CommonConstant.DEFAULT_LANGUAGE;
        }
        return lang;
    }
    
    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
        Map map=new HashMap();
        /*{account=15058505455, oldPwd=muR55gKM, newPwd=Test1234, apName=CMCC-WEB, authRegion=浙江|金华, supplier=金华卡池|xx移动wz, 
         * callback=http://211.154.6.17/v4/ap/accountNotify.do , partnerKey=wifiin, nonce=26632288, timestamp=1453985995082, sign=83oKA8EDFUGNz/T6DOhLdspIFuI=}*/
        //808Fn7RjBMvpHtXQpMDu4tkiv0I=
        map.put("account", "15058505455");
        map.put("oldPwd", "muR55gKM");
        map.put("newPwd", "Test1234");
        map.put("apName", "CMCC-WEB");
        map.put("authRegion", "浙江|金华");
        map.put("supplier", "金华卡池|xx移动wz");
        map.put("callback", "http://211.154.6.17/v4/ap/accountNotify.do");
        map.put("partnerKey", "wifiin");
        map.put("nonce", "26632288");
        map.put("timestamp","1453985995082");
//        map.put("sign", "7CXd0lEicReAMbDBYvtecgHz5Ek=");
        System.out.println(makeSignInParams(map,"0tayexpqSEhvc2YdjIOAafsomtqwuG8t"));
        System.out.println(MessageDigestUtil.md5Base64("wifiwyt_wifiwyt123"));
        //{"sdkPartnerUserId":"15135764","os":"0","osVersion":"4.2.2","signature":"","loginType":"-1",
        //"imei":"99000640020178","verify":"MDExNmJlNjJlccM4NWY0MWZlMTAzYTYzMjY3ZTUyY2FkYWQ=",
        //"sdkPartnerKey":"c0c7c76d30bd3dcaefc96f40275bdc0a","time":"20161203192501018","clientVersion":"2.4.24","deviceId":"99000640020178","mac":"a086c67f9ba0"}
        System.out.println(makeVerifyCode("357555058213093","20170329103556017"));
        System.out.println(verify("357555058213093","20170329103556017","MjlkZGQ4MTZUZIRkOTEyZjdlZjZkMTU0ZWRkZmMyNWEzMzk="));
        System.out.println(MessageDigestUtil.sha1Base64("helloworld"));
    }
}

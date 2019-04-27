package com.ljf.opencvocr.util;

import com.alibaba.fastjson.JSONObject;

/**
 * 身份证工具类
 * @author ljf
 * @since 2019-04-22
 */
public class IdCardUtil {

    public static JSONObject filterOcrInfo(String ocrInfo){
//        System.out.println(ocrInfo);
        ocrInfo = filter(ocrInfo);
        ocrInfo = ocrInfo.replace("\n\n","\n");
        System.out.println(ocrInfo);

        String[] array = ocrInfo.split("\n");

        JSONObject result = new JSONObject();
        //防止前台undefined
        result.put("name","");
        result.put("gender","");
        result.put("nation","");
        result.put("year","");
        result.put("month","");
        result.put("day","");
        result.put("address","");
        result.put("idCode","");

        //找出身份证号码的那行
        int idCodeIndex = 0;
        for(int i = 0;i < array.length;i ++){
            if(i > 4){
                String idCode = idCodeFilter(array[i]);
                if(idCode.length() == 18){
                    idCodeIndex = i;
                    break;
                }
            }
        }

        for(int i = 0;i < array.length;i ++){
            String text = array[i];
            if(i == 0){
                String name = text;
                if(name.contains("名")){
                    int index = name.indexOf("名");
                    name = name.substring(index + 1);
                }else{
                    name = name.replace("\n","");
                }
                name = name.replace("-","");
                result.put("name",nameFilter(filter(name)));
            }
            if(i == 1){
                String nation = text;
                nation = nation.replace(" ","");
                nation = nation.replace("文","汉");
                nation = nation.replace("又","汉");
                nation = nation.replace("叉","汉");
                nation = nation.replace("双","汉");
                nation = nation.replace("汊","汉");
                nation = nation.replace("况","汉");
                for(String n : Constants.NATIONS){
                    if(nation.contains(n)){
                        result.put("nation",n);
                        break;
                    }
                }
            }
            if(i >= 3 && i < idCodeIndex){
                String address = text;
                int aIndex = address.indexOf("址");
                if(aIndex != -1){
                    address = address.substring(aIndex + 1);
                }
                String addr = result.getString("address");
                if(addr != null){
                    addr += address;
                    result.put("address",filter(addr));
                }else{
                    result.put("address",filter(address));
                }
            }

            if(i > 4){
                String idCode = idCodeFilter(text);
                if(idCode.length() == 18){
                    result.put("gender",parseGender(idCode));
                    String year = idCode.substring(6,10);
                    String month = idCode.substring(10,12);
                    String day = idCode.substring(12,14);
                    result.put("year",year);
                    int m = Integer.parseInt(month.substring(0,1));
                    int d = Integer.parseInt(day.substring(0,1));
                    result.put("month",m == 0 ? month.toCharArray()[1] : month);
                    result.put("day",d == 0 ? day.toCharArray()[1] : day);
                    result.put("idCode",idCode.replace("x","X"));
                }
            }

        }

        return result;
    }

    //取出身份证号
    public static String idCodeFilter(String text){
        String temp = text;
        temp = temp.replace(" ", "").
                replace("o", "0").
                replace("O", "0").
                replace("l", "1").
                replace("]", "1").
                replace("】", "1").
                replace("?", "7").
                replace("了", "7").
                replace("B", "8").
                replace("弓", "3").
                replace("引", "3");
        String code = "";
        char[] textArray = temp.toCharArray();
        for(char c : textArray){
            //是否为数字
            boolean isDigit = Character.isDigit(c);
            //后面是否为连续
            if((isDigit || String.valueOf(c).toLowerCase().equals("x")) && code.length() < 18){
                code += c;
                if(code.length() == 18){
                    break;
                }
            }else{
                code = "";
            }
        }
        return code;
    }

    //根据身份证号获取性别
    public static String parseGender(String idCode){
        String gender = "";
        if(idCode.length() == 18){        	
        	String s = idCode.substring(16,17);
        	int i = Integer.parseInt(s);
        	if(i % 2 == 0){
        		gender = "女";
        	}else{
        		gender = "男";
        	}
        }
        return gender;
    }

    //过滤特殊字符
    public static String filter(String text){
        String s = " 《》『』（）()|[]】\"〕′_＿ˇ`~!@#$%^&*+={}':;＇,.<>＜＞\\＼/?～！＃￥％…＆＊＋｛｝‘；：”“’。，、？";
        char[] sArray = s.toCharArray();
        for(char c : sArray){
            text = text.replace(String.valueOf(c),"");
        }
        if(text.lastIndexOf("-") == (text.length() - 1)){
            text = text.substring(0,text.length() - 1);
        }
        return text;
    }

    //名字只能是中文，过滤英文字母和数字
    public static String nameFilter(String name){
        char[] chars = name.toCharArray();
        for(char c : chars){
            if ((c > 'A' && c < 'Z') || (c > 'a' && c < 'z') || Character.isDigit(c)) {
                name = name.replace(String.valueOf(c),"");
            }
        }
        name = name.replace("-","");
        return name.trim();
    }

}

package com.ruoyi.web.controller.village;

import com.ruoyi.framework.web.base.BaseController;
import com.ruoyi.village.domain.VillagerInfo;
import com.ruoyi.village.service.IVillagerInfoService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Author: ZX
 * @Description:
 * @Date: Create in 11:10 2019/11/10
 */
@Controller
@RequestMapping("/village/analysis")
public class AnalysisController extends BaseController {

    private String prefix = "village/analysis";

    @Autowired
    private IVillagerInfoService villagerInfoService;

    @RequiresPermissions("village:analysis:view")
    @GetMapping()
    public String villagerInfo()
    {
        return prefix + "/analysis";
    }

    /**
     * 查询村民并分析比例
     */
    @RequiresPermissions("village:analysis:list")
    @PostMapping("/count")
    @ResponseBody
    public String Groupsexy(String grouptype,String newtime)
    {

        List<VillagerInfo> list = villagerInfoService.selectVillagerInfoListBygrouptype(grouptype);
        Integer count=list.size();//总数
        Integer fnum=0;//男数量
        Integer fmnum=0;//女数量
        for(int i=0;i<count;i++){
            if("男".equals(list.get(i).getSex())) {
                fnum++;
            }else{
                fmnum++;
          }

        }
        Integer agenum1=0; //年龄大于六十
        Integer agenum2=0; //年龄小于十
        Integer agenum3=0; //年龄大于十小于六十
        String idcardnum="";
        for(int k=0;k<count;k++){
            idcardnum=list.get(k).getIdcard();
            if(getAgeByIDNumber(idcardnum)>60){
                agenum1++;
            }
            else if(getAgeByIDNumber(idcardnum)<10){
                agenum2++;
            }
            else if(getAgeByIDNumber(idcardnum)>10&&getAgeByIDNumber(idcardnum)<60){
                agenum3++;
            }

        }
        Integer isdemob=0;
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        for(int j=0;j<count;j++){
            if(list.get(j).getDemobdate()!=null){


                Date date=list.get(j).getDemobdate();
                String demodate=sdf.format(date);
                demodate=demodate.substring(0,4);
                if((list.get(j).getIsdemob()==1)&&yearMonthBetween(demodate,"2018","2019")) {
                    isdemob++;
                }
            }
        }
            Integer islowingcome=0;
           for(int z=0;z<count;z++){

            if(list.get(z).getIslowincome()==1){
                islowingcome++;
            }
           }
        String result;
        if(newtime==""){
            result=count.toString()+'|'+fnum.toString()+'|'+ fmnum.toString()+'|'+agenum1.toString()+'|'+agenum2.toString()+'|'+agenum3.toString()+'|'+isdemob.toString()+'|'+islowingcome.toString();
        }else {
            Integer agenum4 = 0; //新增人口
            for (int o = 0; o < count; o++) {
                idcardnum = list.get(o).getIdcard();
                String date2 = extractYearMonthDayOfIdCard(idcardnum);
                if (newtime.compareTo(date2) < 0) {
                    agenum4++;
                }

            }
            result=count.toString()+'|'+fnum.toString()+'|'+ fmnum.toString()+'|'+agenum1.toString()+'|'+agenum2.toString()+'|'+agenum3.toString()+'|'+isdemob.toString()+'|'+islowingcome.toString()+'|'+agenum4.toString();
        }


        return result;
    }

    private static final int invalidAge = -1;//非法的年龄，用于处理异常。
    //身份证获取年龄
    public static int getAgeByIDNumber(String idNumber) {
        String dateStr;
        if (idNumber.length() == 15) {
            dateStr = "19" + idNumber.substring(6, 12);
        } else if (idNumber.length() == 18) {
            dateStr = idNumber.substring(6, 14);
        } else {//默认是合法身份证号，但不排除有意外发生
            return invalidAge;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            Date birthday = simpleDateFormat.parse(dateStr);
            return getAgeByDate(birthday);
        } catch (ParseException e) {
            return invalidAge;
        }


    }

    public static int getAgeByDate(Date birthday) {
        Calendar calendar = Calendar.getInstance();

        //calendar.before()有的点bug
        if (calendar.getTimeInMillis() - birthday.getTime() < 0L) {
            return invalidAge;
        }


        int yearNow = calendar.get(Calendar.YEAR);
        int monthNow = calendar.get(Calendar.MONTH);
        int dayOfMonthNow = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(birthday);


        int yearBirthday = calendar.get(Calendar.YEAR);
        int monthBirthday = calendar.get(Calendar.MONTH);
        int dayOfMonthBirthday = calendar.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirthday;


        if (monthNow <= monthBirthday && monthNow == monthBirthday && dayOfMonthNow < dayOfMonthBirthday || monthNow < monthBirthday) {
            age--;
        }

        return age;
    }


    public static boolean yearMonthBetween(String nowDate, String startDate, String endDate) {

        int now = Integer.parseInt(nowDate);
        int start=Integer.parseInt(startDate);
        int end=Integer.parseInt(endDate);
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//
//        Date now = format.parse(nowDate);
//        Date start = format.parse(startDate);
//        Date end = format.parse(endDate);
//
//        long nowTime = now.getTime();
//        long startTime = start.getTime();
//        long endTime = end.getTime();

        return now >= start && now <= end;
    }

    /**
     * 身份证的正则表达式^(\d{15}|\d{17}[\dx])$
     * @param id    身份证号获取出生年月
     * @return    生日（yyyy-MM-dd）
     */
    public String extractYearMonthDayOfIdCard(String id) {
        String year = null;
        String month = null;
        String day = null;
        //正则匹配身份证号是否是正确的，15位或者17位数字+数字/x/X
        if (id.matches("^\\d{15}|\\d{17}[\\dxX]$")) {
            year = id.substring(6, 10);
            month = id.substring(10,12);
            day = id.substring(12,14);
//        }else {
//            System.out.println("身份证号码不匹配！");
//            return null;
        }
        return year + "-" + month + "-" + day;
    }


    /**
     * String转Date
     *
     * @param str
     * @return
     */
    public static Date strToDate(String str) {
        if (StringUtils.isEmpty(str)) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");

        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }



}

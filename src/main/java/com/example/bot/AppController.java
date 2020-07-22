package com.example.bot;

import com.example.bot.json.BotResponse;
import com.example.bot.json.BotWebhook;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spring Boot Hello案例
 * <p>
 * Created by bysocket on 26/09/2017.
 */
@RestController
@RequestMapping(value = "/app")
public class AppController {

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String sayHello() {
        return "Hello";
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public BotResponse postBot(@RequestBody BotWebhook webhook) throws IOException {
        System.out.println(webhook);
        BotResponse response = new BotResponse();
        if (webhook != null && webhook.getQueryResult() != null && webhook.getQueryResult().getParameters() != null) {
            String subject = webhook.getQueryResult().getParameters().getSubject();
            if ((subject != null) && !(subject.equals(""))) {
                response.setFulfillmentText(process(subject) );
                response.setSource("something");
            }
        };
        return response;
    }

    private String doQuery(String keyword) throws IOException {

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10L, TimeUnit.SECONDS);
        client.setReadTimeout(10L,TimeUnit.SECONDS);
        Request request = new Request.Builder()
                .url("https://www.asos.com/search/?q=t+shirt")
                .method("GET", null)
//                .addHeader("authority", "www.asos.com")
//                .addHeader("upgrade-insecure-requests", "1")
//                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36")
//                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//                .addHeader("sec-fetch-site", "same-origin")
//                .addHeader("sec-fetch-mode", "navigate")
//                .addHeader("sec-fetch-user", "?1")
//                .addHeader("sec-fetch-dest", "document")
//                .addHeader("referer", "https://www.asos.com/search/?q=t+shirt")
//                .addHeader("accept-language", "he-IL,he;q=0.9,en-US;q=0.8,en;q=0.7")
                //.addHeader("cookie", "browseMVT=nam-block-group90; geocountry=IL; bm_sz=24474B2EC2211129C6C31D9ACB9BB515~YAAQFlBzaOJxmytzAQAAEcPPdwhQS3ejC5/epdESP3/xeSKBmfI0nJzsQ/6nktvxoEm3yqCn8wNzlIx7xs/duHUtAsYLmtMbRhkKeLzISx1jMU6SiOwX+28tK5YLTNE54wD4FGDQ6H2yCWPE7I0vG+dxsp1j1K8jMYeBYfFyhmUUwjUR2WNcEbHyZ5tLog==; check=true; siteChromeVersion=au=11&com=11&de=11&dk=11&es=11&fr=11&it=11&nl=11&pl=11&roe=11&row=11&ru=11&se=11&us=11; keyStoreDataversion=j42uv2x-26; asosAffiliate=affiliateId=17295; browseCountry=IL; browseCurrency=ILS; browseLanguage=en-GB; browseSizeSchema=EU; storeCode=ROW; currency=10046; ak_bmsc=0820636784B26B90F57D4C3F4CF3347868735016E71700001F87185F6332387E~plF8WIAHGJZJbNg7bWpr5Bi81UHprofhtFnQu+vBfQkbnkUMum9IhECQD1aT9JeXtrqhZQqMah1kYUiBZM5Ub7VIT1tX9e0AlFqg+ICYuq8e//olqj2BPLibfLf59YDDZERkS2sDxpnEIDu8ZY7QbVozZqSl475mt0pR5EMywpztb5wE+dchbcrcnU6SFi7Lfu83jKr0lsx3AYTSR2JHu32A0twtZBUYuRVVZfZhcmzsk=; AMCVS_C0137F6A52DEAFCC0A490D4C%40AdobeOrg=1; asos-gdpr22=true; s_ecid=MCMID%7C74897810783582400991650212058022005194; asos-b-sdv629=j42uv2x-26; _abck=33C46FC51DC88FE1C82D903795595CE0~0~YAAQFlBzaOxxmytzAQAA3tXPdwR+yjPBeePTxbRMIeX3Gs5T3oFTqeSD5bdjzlR29bz/z1W0a46r9NZxwrIHSooXiR+fLsbWfNLGpvbg6xGUwYtdfz70afiIay1fqLNuu3turKMZtoi7N/YfN5dHim9hVHcwhsJNBxIyS6BVE5y15do6x+Rxv2KT6CpBj5lNtzKoIbW0VZh2MPxgfRd0JeZXSW+9j2sN9ua0OsmiwwNP8JHcN/CzB4BxaLyjv3lP2Ni3hsTgg+dZLaK9zGes4c5xKSCW9b/yzuQkv/pCXQr9UUegXPSEzcmGAKesuecZ56woqo0=~-1~-1~-1; bm_mi=52259312FECC10C28AF9BA01D7C0BBCB~oWMCEBpx+utp10MBxlzIiw4GoUD/NGl8Cyj3zEnWKRlrPvoawzEK7JoE10WtVNC9XS0lq+ehPJux++VMeHmb0DCxy/JpXkLhzh+dUMQts9KM/UmQ1Nz3VsCtuw+6bWZX/BfUR+Dr4xJ8jFWogTJ4zgevrxI3Cx41NJiJwtMThNYlZnSsltZMBwA2XaHI8C0osjOrLIGIY6t9ZalGQRw3vdJNpoAi+sRWSvVCcAPu5bIe7Bj0yesj9mzG6pdFlX05MeQe98/Iph+Gdi7au5ECh8ql2sNhCbxM/AAX/Mqy/KFxHDglluGPDKFp6V5jHEHl; browseMVT=nam-block-group90; bt_recUser=0; btpdb.ydg7T9K.dGZjLjcxMzA0Nzc=U0VTU0lPTg; asos=PreferredSite=&currencyid=10046&currencylabel=ILS&customerguid=e6566cca60f3459280b436a82aa08cc9; asos-perx=e6566cca60f3459280b436a82aa08cc9||563b750ebf7b44ca932db7166a412083; _s_fpv=true; s_cc=true; bt_stdstatus=NOTSTUDENT; btpdb.ydg7T9K.dGZjLjcwNTk5ODM=U0VTU0lPTg; btpdb.ydg7T9K.dGZjLjcyMzUwNjA=U0VTU0lPTg; _ga=GA1.2.888396032.1595442977; _gid=GA1.2.815631463.1595442977; _gat=1; _fbp=fb.1.1595442977495.665397274; _gcl_au=1.1.1041525796.1595442978; _pin_unauth=dWlkPU1UQmpPRE00WlRBdFkyWTVOUzAwWkdVeUxXRmtNR010TnpkbFpqSm1NR1kxTURNdw; mbox=session#589f874d633b479d895d156ed3ebd3f0#1595444846|PC#589f874d633b479d895d156ed3ebd3f0.37_0#1658687777; s_pers=%20s_vnum%3D1596229200801%2526vn%253D1%7C1596229200801%3B%20gpv_p6%3D%2520%7C1595444776817%3B%20s_invisit%3Dtrue%7C1595444785719%3B%20s_nr%3D1595442985723-New%7C1626978985723%3B%20gpv_e47%3Dno%2520value%7C1595444785727%3B%20gpv_p10%3Ddesktop%2520row%257Csearch%2520page%257Csuccessful%2520search%7C1595444785732%3B; _uetsid=b249011b354c8c08d683c08067338fef; _uetvid=094793d95e632346d2d7ef98bd7e022d; AMCV_C0137F6A52DEAFCC0A490D4C%40AdobeOrg=-1303530583%7CMCIDTS%7C18466%7CvVersion%7C3.3.0%7CMCMID%7C74897810783582400991650212058022005194%7CMCAAMLH-1596047775%7C6%7CMCAAMB-1596047775%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1595450175s%7CNONE%7CMCAID%7CNONE; plp_columsCount=twoColumns; s_sq=asoscomprod%3D%2526c.%2526a.%2526activitymap.%2526page%253Ddesktop%252520row%25257Csearch%252520page%25257Csuccessful%252520search%2526link%253DSearch%2526region%253Dchrome-sticky-header%2526pageIDType%253D1%2526.activitymap%2526.a%2526.c; RT=\"z=1&dm=asos.com&si=8dcb07ab-f44b-40de-b835-3ccbe2cd21c7&ss=kcxpjxim&sl=5&tt=42v&obo=3&bcn=%2F%2F684d0d3b.akstat.io%2F&ld=9c7&nu=82407f7d905279dbcd9919591c54fb7b&cl=zhy&ul=zje\"; _abck=33C46FC51DC88FE1C82D903795595CE0~-1~YAAQLVBzaFopZQ1zAQAAufrRdwSv6MSVHjVCzUljuUDD5kG9SI8QvOf1n8jeC9PNmuKC+z0+BUU0QZ84rcEI+t0LLBIp+2ZzzchqDQSLEIoXE7G/TITVYn6WotQ9ECLitEBOOdDHfM+RcVq02/QFUM8/kxmGKpfA7YoOXygqk3KHlt+q+gLwhi/VGnQSvc+7Jq6qkqBlUM+4KdBOMaf/7NGeSvyK2jWZGdbPwpsN6mFzegBbPAZ75IZs6+rzsqffQ5YHWVrYt+ugF2noTrGZFOmX1h8/SH3YS8fRlHy4NqrUBnZQRrrfO0msq+/tN+S3JRHsWl4=~0~-1~-1")
//                .addHeader("if-none-match", "W/\"583f9-P+d8GE5C6NWinBHtQt6UirKHr9o\"")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private String process(String keyword) throws IOException {
        String res = "";
        String text = doQuery(keyword);
        System.out.println(text.replace('\n',' '));
        Pattern name = Pattern.compile("<div data-auto-id=\"productTileDescription\" class=\"_3WEsAhb\"><div class=\"_3J74XsK\"><div><p>([A-Za-z0-9-. ]+)</p></div></div></div>");
        Pattern price = Pattern.compile("data-auto-id=\"productTile[A-Za-z]+\"(><span)? class=\"_[A-Za-z0-9]+\">([A-Za-z0-9.]+)</span>");
        System.out.println(text.replace("\n", ""));
        String[] prods = (text.replace("\n", "").split("article id=\"product"));

        for (String p : prods) {
            //System.out.println(p);
            Matcher m = name.matcher(p);
            Matcher pr = price.matcher(p);
            if (m.find() && pr.find()) {
                System.out.println(m.group(1) + " price: " + pr.group(2)+"\n");
                res += m.group(1) + " price: " + pr.group(2);
            }

        }
        return res;
    }
}
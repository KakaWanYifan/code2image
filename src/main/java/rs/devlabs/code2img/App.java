package rs.devlabs.code2img;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import rs.devlabs.code2img.Code2ImageSettingsBuilder.Code2ImageSettings;
import rs.devlabs.code2img.themes.Fonts;
import rs.devlabs.code2img.themes.Themes;
import rs.devlabs.code2img.utils.ImageUtils.ImageFormat;

/**
 *
 * @author Milos Stojkovic <iqoologic@gmail.com>
 */
public class App {

    public static void main(String[] args) throws IOException {
        String file = "md.md";
        int three = 0;
        int front = 0;
        int script = 0;
        boolean code = false;
        int codeBlockIndex = 0;
        String url = "";

        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(file + ".md"));

        String line;
        ArrayList<String> codeBlock = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            // 在博客主页隐藏
            if (line.startsWith("hide:")) {
                bw.write("hide: true");
                bw.newLine();
                bw.flush();
                continue;
            }
            // 不提交给搜索引起
            if (line.startsWith("sitemap:")) {
                bw.write("sitemap: false");
                bw.newLine();
                bw.flush();
                continue;
            }
            // 在原本的url后面加'0'
            if (line.startsWith("url:")) {
                bw.write(line + "0");
                bw.newLine();
                bw.flush();

                url = line.split(":")[1].strip();
                continue;
            }
            // 如果front结束，添加这段JS脚本。
            if (line.startsWith("---") && front < 2) {
                front = front + 1;
                if (front == 2) {
                    bw.write("---");
                    bw.newLine();
                    String scriptStr = "<script type=\"text/javascript\">\n" +
                            "    // 如果不是iPhone\n" +
                            "    if(navigator.platform != \"iPhone\"){\n" +
                            "        var url = window.location.href;\n" +
                            "        var replace = url.substr(-7,6);\n" +
                            "        location.replace(replace);\n" +
                            "    }\n" +
                            "    var dom = document.querySelector(\".post-meta-wordcount\");\n" +
                            "    dom.removeAttribute(\"class\");\n" +
                            "    dom.setAttribute(\"style\",\"display:none\");\n" +
                            "</script>";
                    bw.write(scriptStr);
                    bw.newLine();
                    bw.flush();
                    continue;
                }
            }
            // 原博客的script脚本不要
            // script == 0，是为了防止误杀后面真正的脚本
            if (script == 0 && line.startsWith("<script type=\"text/javascript\">")){
                script = script + 1;
            }
            if (script == 1 && line.startsWith("</script>")){
                script = script + 1;
                continue;
            }
            if (script > 0 && script < 2){
                continue;
            }

            // 代码块
            if (line.startsWith("```")){
                three = three + 1;
                if (three % 2 == 1){
                    // 说明是代码起始位置
                    code = true;
                }else{
                    // 说明是代码结束位置
                    code = false;
                }
            }
            if (code){
                if (line.startsWith("```")){
                    codeBlock.clear();
                }else{
                    if (line.length() >= 200){
                        codeBlock.add(line.substring(0,200));
                    }else{
                        codeBlock.add(line);
                    }
                }
            }else {
                if (line.startsWith("```")){
                    codeBlockIndex = codeBlockIndex + 1;
                    String fileName = String.format("%03d", codeBlockIndex);
                    String pngPath = "/-/" + url.substring(0,1) + "/" + url.substring(1,3) + "/" + url.substring(3,5) + "0/";
                    pngPath = pngPath + fileName + ".png";
                    String content = "![" + fileName + "](" + pngPath + ")";
                    System.out.println(pngPath);
                    gePng(codeBlock,fileName);
                    bw.write(content);
                }else {
                    bw.write(line);
                }
                bw.newLine();
                bw.flush();
            }
        }
        bw.close();
        br.close();
    }


    public static void gePng(List<String> codeBlock,String fileName) throws IOException {
        Code2ImageSettingsBuilder settingsBuilder = new Code2ImageSettingsBuilder();
        Code2ImageSettings settings = settingsBuilder
                .setFont(new Font("JetBrains Mono NL", Font.PLAIN, 14))
                .setMargin(10)
                .setRectangleArc(15)
                .setButtonRadius(12)
                .setDrawLineNumbers(true)
                .setDrawCreatedByText(false)
                .setTheme(Themes.BASE16LIGHT)
                .setFormat(ImageFormat.PNG)
                .build();
        Code2Image converter = new Code2Image(settings);
        converter.convertAndSave(codeBlock,new File("./png/" + fileName + "." + ImageFormat.PNG.getExtension()));
    }

}

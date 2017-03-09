package com.wifiin.test.text.template;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.wifiin.util.text.template.TextTemplateFormatterType;

public class TestPlainTextTemplateFormatter{
    @Test
    public void testPlainTextFormatter(){
        String expected="Hello World";
        String template="${h}ello${s}${w}orld";
        Assert.assertEquals(expected,TextTemplateFormatterType.PLAIN_TEXT.formatter(template,"${","}").format(new TemplateData()));
        Map data=Maps.newHashMap();
        data.put("h","H");
        data.put("s"," ");
        data.put("w","W");
        Assert.assertEquals(expected,TextTemplateFormatterType.PLAIN_TEXT.formatter(template,"${","}").format(data));
        template="${h}ello${regex:^\\s$}${w}orld";
        data.put("\t"," ");
        Assert.assertEquals(expected,TextTemplateFormatterType.PLAIN_TEXT.formatter(template,"${","}").format(data));
        template="${}ello${}${}orld";
        Assert.assertEquals(expected,TextTemplateFormatterType.PLAIN_TEXT.formatter(template,"${","}").format(new String[]{"H"," ","W"}));
        template="${0}ello${1}${2}orld";
        Assert.assertEquals(expected,TextTemplateFormatterType.PLAIN_TEXT.formatter(template,"${","}").format(new String[]{"H"," ","W"}));
        template="${1}ello${2}${0}orld";
        Assert.assertEquals(expected,TextTemplateFormatterType.PLAIN_TEXT.formatter(template,"${","}").format(new String[]{"W","H"," "}));
        template="#h#ello#s##w#orld";
        Assert.assertEquals(expected,TextTemplateFormatterType.PLAIN_TEXT.formatter(template,"#","#").format(data));
    }
}

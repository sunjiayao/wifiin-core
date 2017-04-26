package com.wifiin.util.text.template;

/**
 * GROOVY和FREE_MARKER会忽略prefix和suffix
 * @author Running
 */
public enum TextTemplateFormatterType{
    PLAIN_TEXT{
        public TextTemplateFormatter formatter(String template,String prefix,String suffix){
            return TextTemplateFormatterFactory.getPlainTextTemplateFormatter(template,prefix,suffix);
        }
        public TextTemplateFormatter formatterByTAG(String tag){
            return TextTemplateFormatterFactory.getPlainTextTemplateFormatterByTAG(tag);
        }
        @Override
        public TextTemplateFormatter formatter(String template){
            return formatter(template,"${","}");
        }
    },
    FREE_MARKER{
        public TextTemplateFormatter formatter(String template){
            return TextTemplateFormatterFactory.getFreeMarkerTextTemplateFormatter(template);
        }
        public TextTemplateFormatter formatterByTAG(String tag){
            return TextTemplateFormatterFactory.getFreeMarkerTextTemplateFormatterByTAG(tag);
        }
        @Override
        public TextTemplateFormatter formatter(String template,String prefix,String suffix){
            return formatter(template);
        }
    },
    GROOVY{
        public TextTemplateFormatter formatter(String template){
            return TextTemplateFormatterFactory.getGroovyTextTemplateFormatter(template);
        }
        public TextTemplateFormatter formatterByTAG(String tag){
            return TextTemplateFormatterFactory.getGroovyTextTemplateFormatterByTAG(tag);
        }
        @Override
        public TextTemplateFormatter formatter(String template,String prefix,String suffix){
            return formatter(template);
        }
    };
    public abstract TextTemplateFormatter formatter(String template,String prefix,String suffix);
    public abstract TextTemplateFormatter formatterByTAG(String tag);
    public abstract TextTemplateFormatter formatter(String template);
}

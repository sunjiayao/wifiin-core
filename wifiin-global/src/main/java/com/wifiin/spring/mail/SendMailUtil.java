package com.wifiin.spring.mail;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.activation.UnsupportedDataTypeException;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.wifiin.exception.MailSendingException;
import com.wifiin.util.Help;
import com.wifiin.util.text.template.TextTemplateFormatter;
import com.wifiin.util.text.template.TextTemplateFormatterType;

public class SendMailUtil implements BeanNameAware,Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 347272490534837163L;
	
	private static final Logger logger=LoggerFactory.getLogger(SendMailUtil.class);
	
	private static final Pattern MAIL_CONTENT_KEY=Pattern.compile("#\\w+#");

	private static final Pattern MAIL_SPLITOR=Pattern.compile("\\s*;\\s*");
	private static final String ATTACHMENT_NAME="name";
	private static final String ATTACHMENT_CONTENT="content";
	
	
	
	private JavaMailSender mailSender;
    private MimeMessageHelper msgHelper;
    private String from;
    private String mailto;
    private String cc;
    private String bcc;
    private String contentTemplate;
    private TextTemplateFormatterType formatterType;
    private TextTemplateFormatter contentFormatter;
    private String prefix;
    private String suffix;
    private String subject;
    private String beanId;
    private List<Object> attachments;
    
	public SendMailUtil(){}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	public void setMsgHelper(MimeMessageHelper msgHelper) {
		this.msgHelper = msgHelper;
	}
	/**
	 * 发件人
	 * @param from
	 */
	public void setFrom(String from) {
		this.from = from;
	}
	/**
	 * 邮件正文模板，需要替换的文本由形如#fieldName#的字符串代替。实际发送的邮件正文将按照传入的参数遍历参数MAP的KEY，用KEY对应的值替换#....#
	 * @param contentTemplate
	 */
	public void setContentTemplate(String contentTemplate) {
		this.contentTemplate = contentTemplate;
	}
	public TextTemplateFormatterType getFormatterType(){
        return formatterType;
    }

    public void setFormatterType(TextTemplateFormatterType formatterType){
        this.formatterType=formatterType;
    }
    public String getPrefix(){
        if(Help.isEmpty(prefix)){
            prefix="${";
        }
        return prefix;
    }

    public void setPrefix(String prefix){
        this.prefix=prefix;
    }

    public String getSuffix(){
        if(Help.isEmpty(suffix)){
            suffix="}";
        }
        return suffix;
    }

    public void setSuffix(String suffix){
        this.suffix=suffix;
    }

    /**
	 * 邮件标题
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}
	/**
	 * 邮件正文模板
	 * @return
	 */
	public String getContentTemplate() {
		return contentTemplate;
	}
	public List<Object> getAttachments(){
		return this.attachments;
	}
	public void setAttachments(List<Object> attachments){
		this.attachments=attachments;
	}
	/**
	 * 添加附件，可调用多次
	 * @param attachments 要添加的附件文件
	 */
	public void addAttachment(File... attachments){
		addAttachment(Arrays.asList(attachments));
	}
	/**
	 * 添加附件，可调用多次
	 * @param name    附件名
	 * @param content 附件内容
	 * @throws MessagingException
	 */
	public void addAttachment(String name,InputStream content) throws MessagingException{
		msgHelper.addAttachment(name,new InputStreamResource(content));
	}
	/**
	 * 添加附件，可调用多次
	 * @param name    附件名
	 * @param content 附件内容
	 * @throws MessagingException
	 */
	public void addAttachment(String name,InputStreamSource content) throws MessagingException{
		msgHelper.addAttachment(name,content);
	}
	public void addAttachment(List<File> attachments){
		if(this.attachments==null){
			this.attachments=new ArrayList<>();
		}
		this.attachments.addAll(attachments);
	}

	/**
	 * 邮件正文
	 */
    private ThreadLocal<String> mailContent=new ThreadLocal<String>();
    /**
     * 发送邮件
     */
	public void send(){
    	try {
    		msgHelper.setFrom(this.from);
    		if(Help.isNotEmpty(mailto)){
    			msgHelper.setTo(MAIL_SPLITOR.split(mailto));
    		}
    		if(Help.isNotEmpty(cc)){
    			msgHelper.setCc(MAIL_SPLITOR.split(cc));
    		}
    		if(Help.isNotEmpty(bcc)){
    			msgHelper.setBcc(MAIL_SPLITOR.split(bcc));
    		}
    		msgHelper.setSubject(subject);
    		msgHelper.setText(MAIL_CONTENT_KEY.matcher(Help.convert(mailContent.get(), "")).replaceAll(""),true);
    		msgHelper.setSentDate(new Date());
    		if(Help.isNotEmpty(attachments)){
    			for(int i=0,l=attachments.size();i<l;i++){
    				Object attachment=attachments.get(i);
    				addAttachment(attachment);
    			}
    		}
    		mailSender.send(msgHelper.getMimeMessage());
		} catch (Exception e){
			logger.error("SendMailUtil.send()",e);
			throw new MailSendingException(e.getMessage(),e);
		}
    }
	
	private void addAttachment(Object attachment) throws MessagingException, UnsupportedDataTypeException{
		if(attachment instanceof File){
			File file=(File)attachment;
			msgHelper.addAttachment(file.getName(),file);
		}else if(attachment instanceof Map){
			Map map=(Map)attachment;
			msgHelper.addAttachment((String)map.get(ATTACHMENT_NAME),(InputStreamSource)map.get(ATTACHMENT_CONTENT));
		}else{
			throw new UnsupportedDataTypeException("only File and InputStream could be attachment, but this is "+attachment.getClass());
		}
	}
	/**
	 * 邮件正文，用content的KEY查找正文模板的#....#，并用content的值替换，#是占位符，可以任意指定
	 * @param content
	 * @return
	 */
	public SendMailUtil setContent(Object data){
	    if(data instanceof String){
	        return setContent((String)data);
	    }
		if(contentFormatter==null){
		    synchronized(this){
		        if(contentFormatter==null){
		            contentFormatter=this.formatterType.formatter(this.contentTemplate,getPrefix(),getSuffix());
		        }
		    }
		}
		return setContent(contentFormatter.format(data));
	}
	/**
	 * 完整的邮件正文。邮件正文与邮件正文模板只能用一个
	 * @param content
	 * @return
	 */
	public SendMailUtil setContent(String content){
		mailContent.set(content);
		return this;
	}
	public String getMailto() {
		return mailto;
	}
	/**
	 * 收件人，多个收件人用;分割
	 * @param mailto
	 */
	public void setMailto(String mailto) {
		this.mailto = mailto;
	}
	/**
	 * 抄送
	 * @return
	 */
	public String getCc() {
		return cc;
	}
	/**
	 * 抄送
	 * @param cc
	 */
	public void setCc(String cc) {
		this.cc = cc;
	}
	/**
	 * 密送
	 * @return
	 */
	public String getBcc() {
		return bcc;
	}
	/**
	 * 密送
	 * @param bcc
	 */
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	@Override
	public void setBeanName(String beanId) {
		this.beanId=beanId;
	}
}

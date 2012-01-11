package aurora.plugin.mail;

import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendMail {

	private String ttitle;
	private String tcontent;
	private String smtpServer;
	private String tto;
	private String cto;
	private String tfrom;
	private String password;
	private String userName;
	private String port;

	public void check() {
		if (smtpServer == null || "".equals(smtpServer)) {
			throw new SendMailException("�ռ��˵�ַ����Ϊ��");
		} else if (tfrom == null || "".equals(tfrom)) {
			throw new SendMailException("�����˲���Ϊ�ղ���Ϊ��");
		} else if (password == null || "".equals(password)) {
			throw new SendMailException("���������벻��Ϊ��");
		} else if (tcontent == null || "".equals(tcontent)) {
			throw new SendMailException("�ʼ����ݲ���Ϊ��");
		} else if (tto == null || "".equals(tto)) {
			throw new SendMailException("�ռ��˵�ַ����Ϊ��");
		}
	}

	public void sendMail() throws Exception {
		// JavaMail��ҪProperties������һ��session��������Ѱ���ַ���"mail.smtp.host"������ֵ���Ƿ����ʼ�������.
		// Properties�����ȡ�����ʼ����������û������������Ϣ���Լ�������������Ӧ�ó����� �������Ϣ��

		Properties props = new Properties();
		props.put("mail.smtp.host", smtpServer);// �洢�����ʼ�����������Ϣ
		props.put("mail.smtp.auth", "true");// ͬʱͨ����֤
		props.put("mail.smtp.port", port);

		Session s = Session.getInstance(props, null);// ���������½�һ���ʼ��Ự��null������һ��Authenticator(��֤����)
		// s.setDebug(true);// ���õ��Ա�־,Ҫ�鿴�����ʼ��������ʼ���������ø÷���
		// Message���ʾ�����ʼ���Ϣ���������԰������ͣ���ַ��Ϣ���������Ŀ¼�ṹ��

		Message message = new MimeMessage(s);// ���ʼ��Ự�½�һ����Ϣ����
		Address from = new InternetAddress(tfrom);// �����˵��ʼ���ַ
		message.setFrom(from);// ���÷�����

		// Address to = new InternetAddress(tto);// �ռ��˵��ʼ���ַ
		message.addRecipients(Message.RecipientType.TO, InternetAddress
				.parse(tto));// �����ռ���,���������������ΪTO,����3��Ԥ�����������£�

		if (cto != null && !"".equals(cto)) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress
					.parse(cto));// ���ó���
		}

		message.setSubject(ttitle);// ��������
		message.setSentDate(new Date());// ���÷���ʱ��
		/*
		 * try { message.setDataHandler(new DataHandler(new String(message
		 * .getBytes("utf-8"), "utf-8"), "text/html;charset=utf-8")); } catch
		 * (UnsupportedEncodingException e) { e.printStackTrace(); }
		 */
		// message.setContent(tcontent, "text/html;charset=utf-8");

		Multipart mp = new MimeMultipart();
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setContent(tcontent, "text/html;charset=utf-8");
		mp.addBodyPart(mbp);
		message.setContent(mp);
		message.saveChanges();// �洢�ʼ���Ϣ
		
		// Transport ������������Ϣ�ģ�
		// �����ʼ����շ��������
		Transport transport = null;
		try {
			transport = s.getTransport("smtp");
			transport.connect(smtpServer, userName, password);// ��smtp��ʽ��¼����
			transport.sendMessage(message, message.getAllRecipients());// �����ʼ�,���еڶ�����������������õ��ռ��˵�ַ
		} finally {
			if (transport != null && transport.isConnected()) {
				transport.close();
			}
		}
	}

	public String getTtitle() {
		return ttitle;
	}

	public void setTtitle(String ttitle) {
		this.ttitle = ttitle;
	}

	public String getTcontent() {
		return tcontent;
	}

	public void setTcontent(String tcontent) {
		this.tcontent = tcontent;
	}

	public String getTfrom() {
		return tfrom;
	}

	public void setTfrom(String tfrom) {
		this.tfrom = tfrom;
	}

	public String getTto() {
		return tto;
	}

	public void setTto(String tto) {
		this.tto = tto;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSmtpServer() {
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public String getCto() {
		return cto;
	}

	public void setCto(String cto) {
		this.cto = cto;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}

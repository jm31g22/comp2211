package uk.ac.soton.adauction.example.Utils;

import java.util.Properties;
import java.util.Date;
import javax.mail.*;
import javax.mail.internet.*;

import com.sun.mail.smtp.*;

public class EmailUtils
{
    public static void Email(String mailto, String mailsubject, String mailcontent) throws Exception, MessagingException
    {
        String prot = "smtp";
        String mailer = "smtpsend";
        String mailhost = "smtp.163.com";
        String from = "comp2211cw<comp2211cw@163.com>";
        String user = "comp2211cw";
        String password = "UCjUUa9iJrnPUjwa";
        String to = mailto;
        String subject = mailsubject;
        String cc = null;
        String bcc = null;
        String file = null;
        String record = null;
        String protocol = null;
        String host = null;
        String url = null;
        boolean auth = true;
        boolean verbose = false;
        try {
            Properties props = System.getProperties();
            if(mailhost != null)
                props.put("mail." + prot + ".host", mailhost);
            if(auth)
                props.put("mail." + prot + ".auth", "true");

            Session session = Session.getInstance(props, null);

            Message msg = new MimeMessage(session);
            if (from != null)
                msg.setFrom(new InternetAddress(from));
            else
                msg.setFrom();

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            if (cc != null)
                msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            if (bcc != null)
                msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));

            msg.setSubject(subject);

            if (file != null)
            {
                MimeBodyPart mbp1 = new MimeBodyPart();
                mbp1.setText(mailcontent);
                MimeBodyPart mbp2 = new MimeBodyPart();
                mbp2.attachFile(file);
                MimeMultipart mp = new MimeMultipart();
                mp.addBodyPart(mbp1);
                mp.addBodyPart(mbp2);
                msg.setContent(mp);
            }
            else
            {

                msg.setText(mailcontent);
            }

            msg.setHeader("X-Mailer", mailer);
            msg.setSentDate(new Date());

            SMTPTransport t = (SMTPTransport)session.getTransport(prot);
            try {
                if (auth)
                    t.connect(mailhost, 25, user, password);
                else
                    t.connect();
                t.sendMessage(msg, msg.getAllRecipients());
            }
            finally
            {

                t.close();
            }


            if (record != null)
            {
                Store store = null;
                if (url != null)
                {
                    URLName urln = new URLName(url);
                    store = session.getStore(urln);
                    store.connect();
                }
                else
                {
                    if (protocol != null)
                        store = session.getStore(protocol);
                    else
                        store = session.getStore();

                    if (host != null || user != null || password != null)
                        store.connect(host, user, password);
                    else
                        store.connect();
                }

                Folder folder = store.getFolder(record);
                if (folder == null)
                {
                    System.exit(1);
                }
                if (!folder.exists())
                    folder.create(Folder.HOLDS_MESSAGES);

                Message[] msgs = new Message[1];
                msgs[0] = msg;
                folder.appendMessages(msgs);
            }
        }
        catch (Exception e)
        {
            /*
             * Handle SMTP-specific exceptions.
             */
            e.printStackTrace();
            if (e instanceof SendFailedException)
            {
                MessagingException sfe = (MessagingException)e;
                if (sfe instanceof SMTPSendFailedException)
                {
                    SMTPSendFailedException ssfe = (SMTPSendFailedException)sfe;
                }
                else {} Exception ne;
                while ((ne = sfe.getNextException()) != null && ne instanceof MessagingException)
                {
                    sfe = (MessagingException)ne;
                    if (sfe instanceof SMTPAddressFailedException)
                    {
                        SMTPAddressFailedException ssfe = (SMTPAddressFailedException)sfe;
                    }
                    else if (sfe instanceof SMTPAddressSucceededException)
                    {
                        SMTPAddressSucceededException ssfe = (SMTPAddressSucceededException)sfe;
                    }
                }
            }
            else
            {
                if (verbose)
                    e.printStackTrace();
            }
        }
    }
}

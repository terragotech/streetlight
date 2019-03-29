package com.terragoedge.slvinterface.utils;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.terragoedge.slvinterface.model.InstallationReportModel;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static List<String[]> toStringCsvFile(String filePath) {
        String line = "";
        String cvsSplitBy = ",";
        List<String[]> convertedData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                line = line.replace("null", "");
                line = line.replace("(null)", "");
                line = line.replace("\"", "");
                String[] readData = line.split(cvsSplitBy);
                convertedData.add(readData);
            }
            System.out.print(convertedData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertedData;
    }

    public static void write(String data, String filePath) {
        System.out.println("OutputFile path is :" + filePath);
        System.out.println("OutputFile data is :" + data);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
            // fileOutputStream = new FileOutputStream(filePath + fileName);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.flush();
            System.out.println("Successfully generated CSV file");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("file writting problem" + e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void writeFile(String filepath, String value) {
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filepath);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(value);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                closeBufferedReader(bufferedWriter);
                closeFileReader(fileWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeBufferedReader(BufferedWriter bufferedWriter) throws Exception {
        if (bufferedWriter != null) {
            bufferedWriter.close();
        }
    }

    public static void closeFileReader(FileWriter fileWriter) throws Exception {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }

    public static String getBasePath() {
        String basePath = PropertiesReader.getProperties().getProperty("report.basePath");
        return basePath;
    }

    public static String getReportPath(String reportType,long milli) {
        String reportFolder =FileUtils.getBasePath() + reportType+ "/" + convertMiliToDate(milli) +"/";
        File file = new File(reportFolder);
        if (!file.exists()) {
            file.mkdirs();
            file.setReadable(true);
            file.setWritable(true);
        }
        return reportFolder;
    }

    public static String convertMiliToDate(long milliSecs) {
        // SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("dd_MMM_yyyy");
        Date date = new Date(milliSecs);
        return formatter.format(date);

    }
    public static String zipFiles(String zipFile, String srcDir) {
        try {
            File srcFile = new File(srcDir);
            File[] files = srcFile.listFiles();
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (int i = 0; i < files.length; i++) {
                // create byte buffer
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(files[i]);
                zos.putNextEntry(new ZipEntry(files[i].getName()));

                int length;

                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();

// close the InputStream
                fis.close();

            }
            zos.close();
        }
//
        catch (Exception e)
        {
            return e.getMessage();
        }
        return "Successfully created the zip file"+zipFile;
    }
    public static void writeInstllationReports(List<InstallationReportModel> installationReportModelList, String syncedReportPath) {
        Writer writer = null;
        try {
            //String filePath = "./res.csv";
            writer = new FileWriter(syncedReportPath);
            StatefulBeanToCsv<InstallationReportModel> beanToCsv = new StatefulBeanToCsvBuilder(writer)
                    .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                    .build();
            beanToCsv.write(installationReportModelList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

    }
    public static void sendSlvSyncReport(String filePath,String recipients,String subject,String fileName,String messageContent) {
        try {
            Properties properties = PropertiesReader.getProperties();
            Properties props = System.getProperties();
            final String fromEmail = properties.getProperty("email.id");
            final String emailPassword = properties.getProperty("email.password");

            String host = properties.getProperty("email.host");
            String port = properties.getProperty("email.port");
            String[] to = recipients.split(",", -1);
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.user", fromEmail);
            props.put("mail.smtp.password", emailPassword);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, emailPassword);
                }
            });
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            InternetAddress[] toAddress = new InternetAddress[to.length];
            for (int i = 0; i < to.length; i++) {
                toAddress[i] = new InternetAddress(to[i]);
            }
            for (int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }
            message.setSubject(subject);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(messageContent);
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(filePath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            if(fileName != null){
                messageBodyPart.setFileName(fileName);
            }
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, fromEmail, emailPassword);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package affiniter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserException;

import pxb.android.axml.AxmlVisitor;
import infoflow.axml.AXmlAttribute;
import infoflow.axml.AXmlHandler;
import infoflow.axml.AXmlNode;
import infoflow.axml.ApkHandler; 

public class ApkFile {
   
   protected ZipFile zip;
   protected File apkFile;   
   public enum ComponentType {
      Activity, Service, ContentProvider, BroadcastReceiver
   }
   protected ApkHandler apk = null;
   protected AXmlHandler axml;
   protected AXmlNode manifest;
   protected AXmlNode application;
   protected String packageName;

   // Components in the manifest file
   protected List<AXmlNode> providers = null;
   protected List<AXmlNode> services = null;
   protected List<AXmlNode> activities = null;
   protected List<AXmlNode> receivers = null;   
   
   /* Returns XML in binary format, which must be interpreted later*/
   public InputStream getManifest() throws IOException {
      String am = "AndroidManifest.xml";
      InputStream is = null;           
      
      // search for Manifest file
      Enumeration<?> entries = this.zip.entries();
      while (entries.hasMoreElements()) {
         ZipEntry entry = (ZipEntry) entries.nextElement();
         String entryName = entry.getName();
         if (entryName.equals(am)) {
            is = this.zip.getInputStream(entry);
            break;
         }
      }
      
      return is;
   }
   
   protected void handle(InputStream manifestIS) throws IOException/*, XmlPullParserException*/ {
      this.axml = new AXmlHandler(manifestIS);

      // get manifest node
      List<AXmlNode> manifests = this.axml.getNodesWithTag("manifest");
      if (manifests.isEmpty())
         throw new RuntimeException("Manifest contains no manifest node");
      else if (manifests.size() > 1)
         throw new RuntimeException("Manifest contains more than one manifest node");
      this.manifest = manifests.get(0);

      // get application node
      List<AXmlNode> applications = this.manifest.getChildrenWithTag("application");
      if (applications.isEmpty())
         throw new RuntimeException("Manifest contains no application node");
      else if (applications.size() > 1)
         throw new RuntimeException("Manifest contains more than one application node");
      this.application = applications.get(0);

      // Get components
      this.providers = this.axml.getNodesWithTag("provider");
      this.services = this.axml.getNodesWithTag("service");
      this.activities = this.axml.getNodesWithTag("activity");
      this.receivers = this.axml.getNodesWithTag("receiver");
      
      this.packageName = this.manifest.getAttribute("package").getValue().toString();
               
      //System.out.println(packageName);
   }
   
   public void processAPK (String apkPath)  throws IOException /* , XmlPullParserException*/ {
      
      this.apkFile = new File(apkPath);
      
      // check if file exists and if it's not a directory
      if (!this.apkFile.exists() || this.apkFile.isDirectory())
         throw new RuntimeException(
            String.format("Path %s does not refer to a valid apk file", apkPath));
      
      this.apk = new ApkHandler(apkFile);
      InputStream is = null;
      try {
         is = this.apk.getInputStream("AndroidManifest.xml");
         this.handle(is);
      } finally {
         if (is != null)
            is.close();
      }
      
      // create zip file from apk file   
      this.zip = new ZipFile(this.apkFile);
      
      //this.zip.stream().map(ZipEntry::getName).forEach(System.out::println);
      
      InputStream manifest = getManifest();
     
   }
   
   public String getPackageName() {
      return this.packageName;
   }
   
   
   public ApkFile(String args[]) {
      
      // iterate over arguments and find apk file and path
      for (String s : args) {
         if (s.matches(".*.apk")) {
           try {
              processAPK(s);
           } catch (IOException e) {
              e.printStackTrace();
           }
           
           break; // expecting only one apk file
         }
       }
      
   }



}

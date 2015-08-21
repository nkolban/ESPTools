package kolban;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class ObjectsToCSV {
	class SectionEntry {
		private String sectionName;
		private int size;

		public SectionEntry(String sectionName, int size) {
			this.sectionName = sectionName;
			this.size = size;
		}

		public int getSize() {
			return size;
		}

		public String getSectionName() {
			return sectionName;
		}
	}; // End of class Section Entry

	class FileEntry {
		private String fileName;
		private Map<String, SectionEntry> sectionEntries;

		public FileEntry(String fileName) {
			this.fileName = fileName;
			sectionEntries = new HashMap<>();
		}
		
		public String getFileName() {
			return fileName;
		}

		void addSection(String sectionName, int size) {
			SectionEntry sectionEntry = new SectionEntry(sectionName, size);
			sectionEntries.put(sectionName, sectionEntry);
		}

		void dump() {
			System.out.println("Filename: " + fileName);
			Iterator<String> it = sectionEntries.keySet().iterator();
			while (it.hasNext()) {
				String sectionName = it.next();
				SectionEntry sectionEntry = sectionEntries.get(sectionName);
				System.out.println(String.format("%s - %d", sectionEntry.getSectionName(), sectionEntry.getSize()));
			}
		}

		public int getSize(String sectionName) {
			SectionEntry sectionEntry = sectionEntries.get(sectionName);
			if (sectionEntry == null) {
				return -1;
			}
			return sectionEntry.getSize();
		}

		public Set<String> getSections() {
			return sectionEntries.keySet();
		}
	}; // End of class FileEntry

	private final String COMMAND = "C:/Espressif/xtensa-lx106-elf/bin/xtensa-lx106-elf-objdump.exe";
	String f1 = "C:/Users/IBM_ADMIN/Documents/RaspberryPi/ESP8266/Espruino/EclipseWorkSpace/E1/build/esp8266/ESP8266_WiFi.o";
	private BufferedReader bis;

	public static void main(String[] args) {
		ObjectsToCSV objectsToCSV = new ObjectsToCSV();
		objectsToCSV.run();
	}

	public void skipLine() {
		try {
			bis.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dumpResults(String results[]) {
		for (int i = 0; i < results.length; i++) {
			System.out.println("" + i + ": " + results[i]);
		}
	}

	public FileEntry objectDumpOneFile(String fileName) {
		ProcessBuilder pb = new ProcessBuilder(COMMAND, "--section-headers", fileName);
		try {
			pb.redirectErrorStream(true);

			Process p = pb.start();

			p.waitFor();
			InputStream is = p.getInputStream();
			bis = new BufferedReader(new InputStreamReader(is));
			skipLine();
			skipLine();
			skipLine();
			skipLine();
			skipLine();
			String line = bis.readLine();
			FileEntry fileEntry = new FileEntry(fileName);
			while (line != null) {
				String results[] = line.split("[ ]+");
				// dumpResults(results);
				String sectionName = results[2];
				int size = Integer.parseInt(results[3], 16);
				fileEntry.addSection(sectionName, size);
				// System.out.println(String.format("%s %d", sectionName,
				// size));
				// System.out.println(line);
				skipLine();
				line = bis.readLine();
			}

			return fileEntry;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void run() {
		//String directory = "C:/Users/IBM_ADMIN/Documents/RaspberryPi/ESP8266/Espruino/EclipseWorkSpace/E1/build/esp8266";
		String directory = "C:/Users/IBM_ADMIN/Documents/RaspberryPi/ESP8266/Espruino/EclipseWorkSpace/E1/build/user";
		Iterator<File> it = FileUtils.iterateFiles(new File(directory), new String[] { "o" }, false);
		ArrayList<FileEntry> fileEntriesList = new ArrayList<>();
		HashSet<String> sectionsSet = new HashSet<>();
		while (it.hasNext()) {
			File currentFile = it.next();
			//System.out.println("File: " + currentFile.getAbsolutePath());
			FileEntry fileEntry;
			fileEntry = objectDumpOneFile(currentFile.getAbsolutePath());
			fileEntriesList.add(fileEntry);
			//fileEntry.dump();
			sectionsSet.addAll(fileEntry.getSections());
		}
		// We now have processed all the entries and have the names of the
		// sections in the sections set

		String sectionNameArray[] = sectionsSet.toArray(new String[0]);
		//System.out.println(sectionNameArray.length);
		
		// Build the header line for the data
		Iterator<FileEntry> it3 = fileEntriesList.iterator();
		System.out.print("filename");
		for (int i = 0; i < sectionNameArray.length; i++) {
			System.out.print("," + sectionNameArray[i]);
		}
		System.out.println("");
		
		while (it3.hasNext()) {
			FileEntry fileEntry = it3.next();
			System.out.print(fileEntry.getFileName());

			for (int i = 0; i < sectionNameArray.length; i++) {
				int size = fileEntry.getSize(sectionNameArray[i]);
				if (size < 0) {
					System.out.print(",");
				} else {
					System.out.print("," + size);
				}
			} // End of for each section name
			System.out.println("");
		}

	}
}

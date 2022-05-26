package teste.ci1;

import com.ibm.cics.server.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {

	private static final String LOCAL_CCSID = System.getProperty("com.ibm.cics.jvmserver.local.ccsid");

    public static void main(CommAreaHolder cah){
		Task task = null;
		byte DFHCOMMAREA[] = cah.getValue();
		try{
			task = Task.getTask();
			if (DFHCOMMAREA.length > 0){
				ByteArrayInputStream bais = new ByteArrayInputStream(DFHCOMMAREA);
				BufferedInputStream buff = new BufferedInputStream(bais);
				DataInputStream dis = new DataInputStream(buff);
				int size = dis.readInt();

				byte[] b2=new byte[size];
				for(int i=0; i<size; i++)
					b2[i] = (byte) dis.readUnsignedByte();

				String texto = new String(b2, LOCAL_CCSID);

				byte[] compressed = compress(texto);
				task.out.print("GZIP comprimido: ");
				task.out.print(compressed);
				task.out.println("\nGZIP descomprimido: " + decompress(compressed));

				byte[] volta = new byte[b2.length+compressed.length];
				for(int i=0; i<b2.length; i++)
					volta[i]=b2[i];
				for(int i=b2.length; i<compressed.length; i++)
					volta[i]=compressed[i-b2.length];

				cah.setValue(b2);
			}else{
				task.err.println("Chamado sem commarea");
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public static byte[] compress(String data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		gzip.write(data.getBytes());
		gzip.close();
		byte[] compressed = bos.toByteArray();
		bos.close();
		return compressed;
	}
	
	public static String decompress(byte[] compressed) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
		GZIPInputStream gis = new GZIPInputStream(bis);
		BufferedReader br = new BufferedReader(new InputStreamReader(gis, LOCAL_CCSID));
		StringBuilder sb = new StringBuilder();
		String line;
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		gis.close();
		bis.close();
		return sb.toString();
	}

}

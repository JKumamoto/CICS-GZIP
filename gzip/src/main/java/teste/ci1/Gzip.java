package teste.ci1;

import com.ibm.cics.server.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Gzip {
    /**
     * Main entry point to a CICS OSGi program.
     */
    public static void main(String[] args) throws CicsConditionException {
		Task task = Task.getTask();
		String strTerm = getTerminalString();

		if (strTerm != null){
			String[] termArgs = parseTerminalString(strTerm);

			try{
				byte[] compressed = compress(termArgs[1]);
				task.out.print("GZIP comprimido: ");
				task.out.print(compressed);
				task.out.println("\nGZIP descomprimido: " + decompress(compressed));
			}catch(IOException e){
				e.printStackTrace();
			}
		}
    }

	private static String getTerminalString() throws CicsConditionException    {
        Object pf = Task.getTask().getPrincipalFacility();

        // Are we of a suitable type?
        if ( pf instanceof TerminalPrincipalFacility) {

            // Cast to correct type
            TerminalPrincipalFacility tpf = (TerminalPrincipalFacility) pf;

            // Create a holder object to store the data
            DataHolder holder = new DataHolder();
            try {
                // Perform the receive from the terminal
                tpf.receive(holder);
            }
            catch (EndOfChainIndicatorException e) {
                // Normal operation - ignore this one
            }
            catch (CicsConditionException cce) {
                // Propagate all other problems
                throw cce;
            }

            // Convert the received data into a valid String
            // Assume this is a valid character string in the CICS local CCSID
            String st = holder.getStringValue();
			tpf.clear();
			return st;
        }else {
            // Not a terminal principal facility
            return null;
        }
    }

	private static String[] parseTerminalString(String strTerm){
        // A place to store the output collection
        List<String> args = new ArrayList<>();

        // Tokenize the input string using standard whitespace characters
        StringTokenizer tok = new StringTokenizer(strTerm);

        // Add each of the tokens to the output collection
        while ( tok.hasMoreTokens() ) {
            args.add(tok.nextToken());
        }

        // Convert the collection to an array
        return args.toArray( new String[args.size()] );
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
		BufferedReader br = new BufferedReader(new InputStreamReader(gis, "ISO-8859-1"));
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

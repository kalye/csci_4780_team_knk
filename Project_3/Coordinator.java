import java.io.*;
public class Coordinator{
	private static String[] readConfig(String fileName){
		String configCommands[] = new String[2];
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName))))
		{
			String linef = br.readLine();
			int i = 0;
			while(linef !=null){
				configCommands[i] = linef;
				//System.out.println(linef);
				linef = br.readLine();
				i++;
			}
			
		}
		catch (FileNotFoundException ex){
			System.out.println(ex + "File wasn't found");
		}
		catch(IOException ex){
			System.out.println(ex + "IO Exception reading file");
		}

		return configCommands;
	}

	public static void main(String[] args)
	{
		String test[] = readConfig(args[0]);
		for (String s: test)
		{
			System.out.println(s);
		}
	}
}


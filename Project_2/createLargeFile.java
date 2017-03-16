import java.io.*;
public class createLargeFile
{
	public static void main(String[] args)
	{
		String fileName = args[0];
		int size = Integer.parseInt(args[1]);
		try {


		
		File fe = new File(fileName);
		byte[] fileArray = new byte[size];
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fe));
		bos.write(fileArray);
		bos.close();
	}
	catch (IOException e) {
            System.out.println("Exception caught when trying to read file: " + fileName);
            System.out.println(e.getMessage());
        } catch (NullPointerException e) {
            System.out.println("Exception caught when trying to read file: " + fileName);
            System.out.println(e.getMessage());
        }
    
}
}
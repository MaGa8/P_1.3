package models;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

import models.Matrix.*;



public class ShapeParser 
{
	@SuppressWarnings("serial")
	public class BadFileStructureException extends Exception
	{
		public BadFileStructureException() {}
		
		public BadFileStructureException (String message) { super (message); }
	}
	
	
	public ShapeParser (File input) throws FileNotFoundException
	{
		mRead = new BufferedReader (new FileReader (input));
		mBlocks = new ArrayList<Block>();
	}
	
	public ArrayList <Block> getBlocks()
	{
		ArrayList <Block> blocks = new ArrayList <Block>();
		for (Block b : mBlocks)
			blocks.add (b.clone());
		return blocks;
	}
	
	public ArrayList <BasicShape> getShapes()
	{
		ArrayList <BasicShape> shapes = new ArrayList <BasicShape>();
		for (Block b : mBlocks)
			shapes.add (new BasicShape (b));
		return shapes;
	}
	
	public void parse() throws BadFileStructureException, IOException
	{
		while (mRead.ready())
		{
			//skip unusable part
			while (mRead.ready() && !(mRead.readLine().equals(new String ("new")))) {}
			
			ArrayList <IntegerMatrix> vecs = parseVectors();
			IntegerMatrix adjacent = parseConnections (vecs);
			double value = Double.parseDouble(mRead.readLine());
			mBlocks.add (new Block (vecs, adjacent, value));
			if (!mRead.readLine().startsWith("end"))
				throw new BadFileStructureException ("missing terminating 'end' token");
		}
	}
	
	private ArrayList <IntegerMatrix> parseVectors() throws IOException
	{
		ArrayList <IntegerMatrix> vecs = new ArrayList<IntegerMatrix>();
		String line = mRead.readLine();
		while (mRead.ready() && !line.startsWith("connect"))
		{
			Scanner scan = new Scanner (line);
			scan.skip("[(]");
			scan.useDelimiter("[,)]");
			ArrayList <Double> entries = new ArrayList <Double>();
			while (scan.hasNextDouble())
				entries.add (scan.nextDouble());
			
			DoubleMatrix vec = new DoubleMatrix (entries.size(), 1);
			for (int cEntry = 0; cEntry < entries.size(); ++cEntry)
				vec.setCell (cEntry, 0, entries.get (cEntry));
			vecs.add (vec.toIntegerMatrix());
			line = mRead.readLine();
			scan.close();
		}
		return vecs;
	}
	
	private IntegerMatrix parseConnections (ArrayList <IntegerMatrix> vecs) throws IOException, BadFileStructureException
	{
		IntegerMatrix adj = new IntegerMatrix (vecs.size(), vecs.size());
		String line = null;
		if (mRead.ready())
			 line = mRead.readLine();
		//read in lines
		while (mRead.ready() && !line.startsWith("value"))
		{
			Scanner scanFragment = new Scanner (line);
			scanFragment.useDelimiter(",");
			//read in comma-separated fragments
			while (scanFragment.hasNext())
			{
				Scanner scanSingle = new Scanner (scanFragment.next());
				scanSingle.useDelimiter("[-,]");
				int prevIndex = -1, currIndex = -1;
				//read in indices
				while (scanSingle.hasNextInt())
				{
					prevIndex = currIndex;
					currIndex = scanSingle.nextInt();
					if (currIndex < 0 || currIndex > vecs.size() - 1)
					{
						scanSingle.close();
						scanFragment.close();
						throw new BadFileStructureException("Index read in does not correspond to a vector!");
					}
					if (prevIndex != -1)
					{
						adj.setCell(prevIndex, currIndex, 1);
						adj.setCell(currIndex, prevIndex, 1);
					}
				}
				scanSingle.close();
			}
			scanFragment.close();
			line = mRead.readLine();
		}
		
		
		return adj;
	}
	
	
	
	private BufferedReader mRead;
	private ArrayList<Block> mBlocks;
}

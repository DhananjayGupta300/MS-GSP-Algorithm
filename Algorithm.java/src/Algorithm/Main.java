/**
 * 
 */
/**
 * @author Karan Kadakia and Dhananjay Gupta
 *
 */
//packaging of the class

package Algorithm;


// importing other classes
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Algorithm.Sequence.CreateWithoutMinMisMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.*;
import java.util.HashMap;
import java.io.PrintWriter;



//the main class of the project
public class Main
{
	
	
	public Parameters param = null;;
	public CollectionSequence seq = null;
	
	//TODO: Please update the file paths
	//entry point to the code
	public static void main(String[] arguments) throws IOException {
		
		Main mainProgram = new Main();
		//TODO: Please update the file path
		String paramsPath = "C:\\Users\\Dhananjay Gupta\\Desktop\\Algorithm.java\\src\\data\\params.txt";
		//TODO: Please update the file path
		String inputDataPath = "C:\\Users\\Dhananjay Gupta\\Desktop\\Algorithm.java\\src\\data\\inputdata.txt";
		//TODO: Please update the file path
		String outFilePath = "C:\\Users\\Dhananjay Gupta\\Desktop\\Algorithm.java\\src\\data\\out.txt";

		
		
        boolean trueOrNot = mainProgram.execute( paramsPath, inputDataPath, outFilePath );
		if (trueOrNot){
			//If the output is generated then we get this message
			System.out.println("The MSGSP algorithm is sucessfull, Please check the output file!!!!");
		}
		else{
			System.out.println("The algorithm did not work as expected!!");
		}
	
	}

	
	//constructor for the main Program class
	public Main()
	{
	}
	
	// This method initiates all the code within the algorithm
	public boolean execute( String misPath, String dataPath, String outputPath )
	{
		boolean secondTrueOrNot = false;
		//stores the parameters as passed when parsing the parameter file
		param = paraFileParser(misPath);
		//stores the parameter as passed when parsing the data file
		seq = dataFileParser(dataPath);
		//if there was something out of parsing the file: do this
		if( (param != null) && (seq != null) )
		{
			//call the MsGspalgirthm on the parsed data
			AlgorithmMSGSP algorithm = new AlgorithmMSGSP( param, seq );
			CollectionSequence patternCollection = algorithm.mineMethod();
			if( patternCollection != null )
			{	
				//save the output to the output file
				patternCollection.Save( outputPath );
				secondTrueOrNot = true;
			}
		}
		//The variable is used to show success or failure on the console window
		return secondTrueOrNot;
	}
	
	
	private Parameters paraFileParser(String paraFilePath)
	{
		Parameters newParameters = null;
		
		try
		{
			FileInputStream parameterFileStream = new FileInputStream(paraFilePath);
			BufferedReader parameterFileBuffer = new BufferedReader(new InputStreamReader(parameterFileStream));
			newParameters = new Parameters();
		 	String line = null;
		 	while ((line = parameterFileBuffer.readLine()) != null) {
	//			System.out.println(line);
		 		//if the line holds mis value
				if(line.contains("MIS")){
					Pattern pattern = Pattern.compile("MIS\\((\\d+)\\) = ([.\\d]+)");
					Matcher matcher = pattern.matcher(line);
					matcher.find();
					newParameters.m_MisTable.addMIS(new Integer(matcher.group(1)), new Float(matcher.group(2)));
	//				System.out.println(m.group(1) + " " + m.group(2));
				}
				//if the line holds SDC
				else if( line.contains( "SDC" ) ){
					Pattern pattern = Pattern.compile("SDC = ([.\\d]+)");
					Matcher matcher = pattern.matcher(line);
					matcher.find();
					newParameters.setSDC(new Float(matcher.group(1)));
	//				System.out.println(m.group(1));
				}
				
		 	}
		 	//closing the file
		 	parameterFileBuffer.close();
		}
		catch( IOException e )
		{	
			//if any exception occurs parameters are set to null
			newParameters = null;
		}
		
	 	return newParameters;
	}
	
	
	
	private CollectionSequence dataFileParser(String dataFilePath)
	{
		CollectionSequence sequenceCol = null;
		
		try
		{
			FileInputStream dataFileStream = new FileInputStream(dataFilePath);
			BufferedReader dataFileBuffer = new BufferedReader(new InputStreamReader(dataFileStream));
	
		 	String line = null;
		 	sequenceCol = new CollectionSequence( param.m_MisTable );
		 	while ((line = dataFileBuffer.readLine()) != null) {
				sequenceCol.AddSequenceFromString(line);
			}
		 
			dataFileBuffer.close();
		}
		catch( IOException e )
		{
			sequenceCol = null;
		}
		
		return sequenceCol;
	}

}






class AlgorithmMSGSP
{
	protected Parameters my_Params = null;
	protected CollectionSequence my_Seq = null;
	
	
	//constructor for the class
	
	public AlgorithmMSGSP(Parameters params, CollectionSequence seq)
	{
		my_Params = params;
		my_Seq = seq;
	}

	
	
	// with the given parameters, mines the current sequence
	public CollectionSequence mineMethod()
	{
		List<CollectionSequence> my_FrequentSeq = new ArrayList<CollectionSequence>(); 
		
		int[] itemIdsSorted = GetItemsSortedByMis();
		
		
		// Fetching properties of the original sequence set
		int iNumSeq = my_Seq.GetNumSeq();
		Map<Integer, Integer> supportCount = my_Seq.GetSupportCount(itemIdsSorted);
		int iSizeOfSequences = my_Seq.GetSize();
		
		
		
		// additional setup
		List<Integer> List = InitPass( itemIdsSorted,  supportCount);
		CollectionSequence freqLv1 = GetLv1FrequentSequences( List, iNumSeq, supportCount );
		my_FrequentSeq.add( freqLv1 );
		

		
		for( int k = 1; my_FrequentSeq.get( k - 1 ).GetNumSeq() > 0; k++ )
		{
			// Generate some frequent sequences for this level
			CollectionSequence freqSeqs = new CollectionSequence( my_Params.m_MisTable );
			CollectionSequence candidateSeqs = null;
			
			
			// Get a collection of candidate sequences for this level
			if( k == 1 )
			{
				candidateSeqs = Lv2CandidateGenSPM( List, supportCount, iNumSeq );
			}
			else
			{
				candidateSeqs = MsCandidateGenSPM( my_FrequentSeq.get( k - 1 ), iNumSeq, supportCount );
			}
			
			
			
			// Remove infrequent items
			for( Sequence s : my_Seq.m_Sequences )
			{
				for( Sequence c : candidateSeqs.m_Sequences )
				{
					if( s.ContainsSequence( c ) )
					{
						c.m_iCount++;
					}


					List<Sequence> minMisReducedSequences = c.CreateSequencesWithoutMinMisItem( itemIdsSorted, CreateWithoutMinMisMethod.Cwm_RemoveFirstOccuranceOnly );
					for( Sequence minMisReducedSequence : minMisReducedSequences )
					{
						if( s.ContainsSequence( minMisReducedSequence ) )
						{
							// Need to increment the count for the sequence that matches exactly minMinRedSeq
							// Find all matching sequences
							CollectionSequence seqLevel = my_FrequentSeq.get( minMisReducedSequence.getLength() - 1 );
							Sequence matchingSequence = seqLevel.FindSequence( minMisReducedSequence );
							if( matchingSequence != null )
							{
								matchingSequence.m_iCount++;
							}
						}
					}
				}
			}
			
			
			
			// Add appropriate candidates into the frequent collection
			for( Sequence c : candidateSeqs.m_Sequences )
			{
				//System.out.println(c + ":   " + (float)c.m_iCount / (float)iSizeOfSequences);
				if( ((float)c.m_iCount / (float)iSizeOfSequences) >= my_Params.m_MisTable.getMIS( c.GetMinMisItem( itemIdsSorted ) ) )
				{
					freqSeqs.AddSequenceWithoutDup( c );
				}
			}
			
			
			my_FrequentSeq.add( freqSeqs );
		}
			
			
		
		// Union all frequent sequences into a total collection
		CollectionSequence totalCollections = new CollectionSequence( my_Params.m_MisTable );
		for( CollectionSequence sc : my_FrequentSeq )
		{
			totalCollections.AddCollection( sc );
		}
		
		return totalCollections;
	}
	
	
	
	private CollectionSequence MsCandidateGenSPM( CollectionSequence sequenceCol, int iNumSequences, Map<Integer,Integer> supportCount)
	{
		CollectionSequence nextCol = MSCandidateGenSPM.join( sequenceCol, my_Params.m_MisTable );
		MSCandidateGenSPM.prune( nextCol,  sequenceCol, my_Params.m_MisTable, my_Params.SDC, iNumSequences, supportCount );
		
		return nextCol;
	}



	// Generates a candidate frequent set for level 2
	private CollectionSequence Lv2CandidateGenSPM( List<Integer> L, Map<Integer, Integer> supportCount, int n )
	{
		CollectionSequence candCol = new CollectionSequence( my_Params.m_MisTable );
		
		
		// Go through all items (L) which is sorted in MIS order
		for( int i = 0; i < L.size(); i++ )
		{
			int l = L.get(i);
			float fSup_l = ((float)supportCount.get( l )) / (float)n;
			
			if( fSup_l >= my_Params.m_MisTable.getMIS( l ) )
			{
				for( int j = i + 1; j < L.size(); j++ )
				{
					int h = L.get(j);
					float fSup_h = ((float)supportCount.get( h )) / (float)n;
					if( ( fSup_h >= my_Params.m_MisTable.getMIS( l )) &&
						(Math.abs( fSup_h - fSup_l ) <= my_Params.SDC) )
					{
						// Add <{x, y}>
						Sequence s = new Sequence();
						ItemSet is = new ItemSet( my_Params.m_MisTable );
						is.addItem( l );
						is.addItem( h );
						s.m_ItemSets.add( is );
						candCol.AddSequenceWithoutDup( s );
						
						// Add <{x}, {y}>
						s = new Sequence();
						s.m_ItemSets.add( new ItemSet( l, my_Params.m_MisTable ) );
						s.m_ItemSets.add( new ItemSet( h, my_Params.m_MisTable ) );
						candCol.AddSequenceWithoutDup( s );
						
						// Add <{y}, {x}>
						s = new Sequence();
						s.m_ItemSets.add( new ItemSet( h, my_Params.m_MisTable ) );
						s.m_ItemSets.add( new ItemSet( l, my_Params.m_MisTable ) );
						candCol.AddSequenceWithoutDup( s );
						
						// Add <{x}, {x}>
						s = new Sequence();
						s.m_ItemSets.add( new ItemSet( l, my_Params.m_MisTable ) );
						s.m_ItemSets.add( new ItemSet( l, my_Params.m_MisTable ) );
						candCol.AddSequenceWithoutDup( s );
					}
				}
			}
		}
		
		return candCol;
	}



	// Gets the first level of frequent item sets
	protected CollectionSequence GetLv1FrequentSequences( List<Integer> L, int iNumSequences, Map<Integer, Integer> supportCount )
	{
		CollectionSequence freq = new CollectionSequence( my_Params.m_MisTable );
		
		for( int iItemId : L )
		{
			int iItemSupportCount = supportCount.get( iItemId );
			float fItemMis = my_Params.m_MisTable.getMIS( iItemId );
			if( ((float)iItemSupportCount / (float)iNumSequences) >= fItemMis )
			{
				Sequence s = new Sequence();
				s.m_ItemSets.add( new ItemSet( iItemId, my_Params.m_MisTable ) );
				s.m_iCount = iItemSupportCount;
				freq.AddSequenceWithoutDup( s );
			}
		}
		
		return freq;
	}
	

	
	
	
	// Generates seeds from the original sequences
	protected List<Integer> InitPass( int[] sortedItemIds, Map<Integer, Integer> supportCount )
	{
		List<Integer> L = new ArrayList<Integer>();
		
		int iNumSequences = my_Seq.GetNumSeq();
		
		int iFirstAcceptedItem = -1;
		
		for( int i = 0; i < sortedItemIds.length; i++ )
		{
			int iItemId = sortedItemIds[i];
			
			if( ((float)supportCount.get( iItemId ) / (float)iNumSequences) >= my_Params.m_MisTable.getMIS( iFirstAcceptedItem == -1 ? iItemId : iFirstAcceptedItem ) )
			{
				if( iFirstAcceptedItem == -1 )
				{
					iFirstAcceptedItem = iItemId;
				}
				L.add( iItemId );
			}
		}
		
		return L;
	}
	
	
	
	// Sorts all item ids by their MIS levels
	protected int[] GetItemsSortedByMis()
	{
		int[] sortedIds = null;
		
		// Pull all info from the MIS table
		List<Pair<Integer, Float>> pairs = new ArrayList<Pair<Integer, Float>>();
		for( Entry<Integer, Float> e : my_Params.m_MisTable.m_Table.entrySet() )
		{
			pairs.add( new Pair<Integer, Float>( e.getKey(), e.getValue() ) );
		}
		
		
		// Start sorting by second
		int i = 0;
		while( i < pairs.size() - 1 )
		{
			if( pairs.get(i).GetSecond() > pairs.get(i + 1).GetSecond() )
			{
				// Swap
				Pair<Integer, Float> temp = pairs.get(i);
				pairs.set(i, pairs.get(i + 1) );
				pairs.set(i + 1, temp );
				i--;
			}
			else
			{
				i++;
			}
			if( i < 0 ) { i = 0; }
		}
		
		
		// Make an array of item ids out of this
		sortedIds = new int[pairs.size()];
		for( i = 0; i < pairs.size(); i++ )
		{
			sortedIds[i] = pairs.get(i).GetFirst();
		}
		
		return sortedIds;
	}
	
	

}





class ItemSet {
	private LinkedList<Integer> m_ItemIds = new LinkedList<Integer>();
	
	private MisTable m_MisTable = null;
	
	public enum ContainsRes
	{
		Cr_No,
		Cr_Partial,
		Cr_Yes,
	}
	
	
	public ItemSet( MisTable misTable )
	{
		m_MisTable = misTable;
	}
	
	
	public ItemSet(LinkedList<Integer> itemIds, MisTable misTable)
	{
		this( misTable );
		m_ItemIds = itemIds;
	}

	public ItemSet( int iItemId, MisTable misTable )
	{
		this( misTable );
		m_ItemIds.add( iItemId );
	}
	
	
	
	public void addItem( int iItemId )
	{
		// Has to be put in MIS order
		ListIterator<Integer> listIterator = m_ItemIds.listIterator();
		int i = 0;
        while (listIterator.hasNext())
        {
        	int iListItemId = listIterator.next();
            if( m_MisTable.getMIS( iItemId ) < m_MisTable.getMIS( iListItemId ) )
            {
            	m_ItemIds.add(i, iItemId);
            	return;
            }
            i++;
        }
        
        // Add to the end
		m_ItemIds.add( iItemId );
	}
	
	
	// Takes the last item out of the item list
	public int pollLast()
	{
		int iRet = 0;
		
		if( m_ItemIds.size() > 0 )
		{
			iRet = m_ItemIds.get( m_ItemIds.size() - 1 );
			m_ItemIds.remove( m_ItemIds.size() - 1 );
		}
		
		return iRet;
	}
	
	
	// Takes the first item out of the item list
	public int pollFirst()
	{
		int iRet = 0;
		
		if( m_ItemIds.size() > 0 )
		{
			iRet = m_ItemIds.get( 0 );
			m_ItemIds.remove( 0 );
		}
		
		return iRet;
	}
	
	
	public int last()
	{
		if( m_ItemIds.size() > 0 )
		{
			return m_ItemIds.get( m_ItemIds.size() - 1 );
		}
		return 0;
	}
	
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( '{' );
		for( int i = 0; i < m_ItemIds.size(); i++ )
		{
			sb.append( m_ItemIds.get(i) );
			if( i < m_ItemIds.size() - 1 )		{ sb.append( ',' ); }
		}
		sb.append( '}' );
		
		return sb.toString();
	}


	public int getLength() {
		return m_ItemIds.size();
	}

	
	
	// Sort by the MIS
	public void SortItemsByMis(MisTable misTable)
	{
		int i = 0;
		while( i < m_ItemIds.size() - 1 )
		{
			if( misTable.getMIS( m_ItemIds.get(i) ) > misTable.getMIS( m_ItemIds.get(i + 1) ) )
			{
				// Swap
				int iTemp = m_ItemIds.get(i);
				m_ItemIds.set(i, m_ItemIds.get(i + 1) );
				m_ItemIds.set(i + 1, iTemp );
				i--;
			}
			else
			{
				i++;
			}
			if( i < 0 ) { i = 0; }
		}
			
		
	}


	
	// Returns true if this item set contains an item id
	public boolean ContainsItem( int iItemId )
	{
		for( int i : m_ItemIds )
		{
			if( i == iItemId )
			{
				return true;
			}
		}
		return false;
	}


	
	// Copy of this item set
	public ItemSet Copy()
	{
		ItemSet copy = new ItemSet( m_MisTable );
		
		for( int i = 0; i < m_ItemIds.size(); i++ )
		{
			copy.m_ItemIds.add( m_ItemIds.get( i ) );
		}
		
		return copy;
	}


	
	// Removes an item from the set
	public void RemoveItemId(int iItemId )
	{
		for( int i = 0; i < m_ItemIds.size(); i++ )
		{
			if( m_ItemIds.get(i) == iItemId )
			{
				m_ItemIds.remove( i );
				break;
			}
		}
	}
	
	
	
	public boolean equals(ItemSet is2){
		if(m_ItemIds.size()!=is2.m_ItemIds.size())
			return false;
		for( int i = 0; i < m_ItemIds.size(); i++ )
		{
			if( m_ItemIds.get(i).intValue() != is2.m_ItemIds.get(i).intValue() )
			{
				return false;
			}
		}
		return true;
	}
	
	
	
	// Returns true if this item set contains the given item set
	public ContainsRes ContainsItems( ItemSet is )
	{
		int iMatchingNums = 0;
		
		for( int i = 0; i < m_ItemIds.size(); i++ )
		{
			for( int j = 0; j < is.m_ItemIds.size(); j++ )
			{
				if( m_ItemIds.get( i ).intValue() == is.m_ItemIds.get( j ).intValue() )
				{
					iMatchingNums++;
					break;
				}
			}
		}
		
		if( iMatchingNums == is.m_ItemIds.size() ) { return ContainsRes.Cr_Yes; }
		else if( iMatchingNums != 0 ) { return ContainsRes.Cr_Partial; }
		
		return ContainsRes.Cr_No;
	}


	public List<Integer> GetItems() {
		return m_ItemIds;
	}


	public int GetItemAtIndex(int i)
	{
		return m_ItemIds.get( i );
	}


	
	// Removes an item at the given index
	public void RemoveItemByIndex(int iIndex )
	{
		if( iIndex < m_ItemIds.size() )
		{
			m_ItemIds.remove( iIndex );
		}
	}


	
	// Returns true if there are no items in this item set
	public boolean IsEmpty()
	{
		return m_ItemIds.size() == 0;
	}


	
	// Compares two item sets with an ignore index
	public boolean isEqual(ItemSet is2, int iIgnoreIndex1, int iIgnoreIndex2)
	{
		ItemSet is1 = this;
		
		if( (is1.getLength() - (iIgnoreIndex1 != -1 ? 1 : 0)) != (is2.getLength() - (iIgnoreIndex2 != -1 ? 1 : 0)) )
		{
			return false;
		}
		
		int iPtr1 = (iIgnoreIndex1 == 0 ? 1 : 0);
		int iPtr2 = (iIgnoreIndex2 == 0 ? 1 : 0);
		
		while( iPtr1 < is1.m_ItemIds.size() )
		{
			if( is1.m_ItemIds.get( iPtr1 ).intValue() != is2.m_ItemIds.get( iPtr2 ).intValue() )
			{
				return false;
			}
			
			iPtr1++;
			iPtr2++;
			if( iPtr1 == iIgnoreIndex1 ) { iPtr1++; }
			if( iPtr2 == iIgnoreIndex2 ) { iPtr2++; }
		}
		
		return true;
	}
}




class CollectionSequence {

	protected MisTable m_MisTable;
	
	public CollectionSequence( MisTable misTable )
	{
		m_MisTable = misTable;
	}
	
	
	public List<Sequence> m_Sequences = new ArrayList<Sequence>();

	
	public void AddSequenceWithoutDup(Sequence seq)
	{
		// Check if this already exists in the collection
		if( !ifContains(seq) )
		{
			m_Sequences.add( seq );
		}
	}
	
	public void AddSequenceWithDup(Sequence seq)
	{
		m_Sequences.add( seq );
	}

	public boolean AddSequenceFromString( String str )
	{
		AddSequenceWithDup( Sequence.CreateSequenceFromString( str, m_MisTable ) );
		
		return true;
	}

	
	
	// Saves this collect to file
	public void Save(String outputPath)
	{
		// Put sequences in length ordered map
		Map<Integer, List<Sequence>> orderedSeq = new HashMap<Integer, List<Sequence>>();
		for( Sequence sequence : m_Sequences )
		{
			int iSequenceLen = sequence.getLength();
			
			List<Sequence> bin = null;
			if( orderedSeq.containsKey( iSequenceLen ) )
			{
				bin = orderedSeq.get( iSequenceLen );
			}
			if( bin == null )
			{
				bin = new ArrayList<Sequence>();
				orderedSeq.put( iSequenceLen, bin );
			}
			
			bin.add( sequence );
		}	

		
		// Make a string of all patterns
		StringBuilder masterStr = new StringBuilder();
		for( Map.Entry<Integer, List<Sequence>> kvp : orderedSeq.entrySet() )
		{
			if( masterStr.length() > 0 ) { masterStr.append( '\n' ); }
			
			masterStr.append( "The number of length " + kvp.getKey() + " sequential patterns is " + kvp.getValue().size() + "\n" );
			
			for( Sequence s : kvp.getValue() )
			{
				masterStr.append( "Pattern:  " + s.toString() + "\n" );
			}
		}
		
		
		// All written to the string, commit to file 
		MiscelaniousFunctions.SaveFileAsString( masterStr.toString(), outputPath );
	}
	
	

	// Gets the count of each sequence that counts an item in the collection
	public Map<Integer,Integer> GetSupportCount(int[] sortedItemIds)
	{
		Map<Integer,Integer> ret = new HashMap<Integer,Integer>();
		
		for( int itemID : sortedItemIds )
		{
			ret.put(itemID, 0);
			for( Sequence sequence : m_Sequences )
			{
				if(sequence.ContainsItem(itemID))
				{
					ret.put( itemID, ret.get( itemID ) + 1 );
					continue;
				}
			}
		}
		
		return ret;
	}

	public int GetNumItemSets() {
		
		int iNumItemSets = 0;
		
		for( Sequence sequence : m_Sequences )
		{
			iNumItemSets += sequence.m_ItemSets.size();
		}
		
		return iNumItemSets;
	}

	
	
	// Add a collection into this one
	public void AddCollection(CollectionSequence sc)
	{
		for( Sequence s : sc.m_Sequences )
		{
			AddSequenceWithDup( s );
		}
	}

	
	
	public int GetNumSeq()
	{
		return m_Sequences.size();
	}

	
	public Sequence GetSequence(int i) {
		return m_Sequences.get( i );
	}

	
	
	public int GetSize() {
		return GetNumSeq();
	}
	
	
	public boolean ifContains(Sequence seq){
	
		for (int i = 0 ; i<m_Sequences.size() ; i++){
			if(m_Sequences.get(i).isEqual(seq)){
				return true;
			}
		}
		return false;
	}

	
	
	// Finds a sequence that matches the given one
	public Sequence FindSequence(Sequence s)
	{
		for( int i = 0; i < m_Sequences.size(); i++ )
		{
			if( m_Sequences.get(i).isEqual( s ) )
			{
				return m_Sequences.get(i);
			}
		}
		
		return null;
	}
		
}




class MSCandidateGenSPM {

	public static void prune(CollectionSequence F, CollectionSequence prevF, MisTable misTable, double SDC, int iNumSequences, Map<Integer,Integer> supportCount ){
		for(int i = 0 ; i<F.m_Sequences.size() ; i++)
		{
			Sequence seq  = F.m_Sequences.get(i);
			
			double diff = seq.getMinMaxSupprtDiff(supportCount, iNumSequences);

			if( diff > SDC )
			{
				F.m_Sequences.remove(i);
				i--;
				continue;
			}
			int minIndex = seq.getExplicitMinMIS( misTable );
			for(int j = 0 ; j < seq.getLength() ; j++)
			{
				if(j==minIndex)
					continue;
				Sequence testSeq = seq.getClone();
				testSeq.RemoveItemByIndex(j);
				if(!prevF.ifContains(testSeq))
				{
					F.m_Sequences.remove(i);
					i--;
					break;
				}
			}
		}
	}
	
	
	public static CollectionSequence join( CollectionSequence F, MisTable misTable ){
		CollectionSequence newFS = new CollectionSequence( misTable );
		for(int i = 0 ; i<F.m_Sequences.size() ; i++){
			for(int j = 0 ; j<F.m_Sequences.size() ; j++){
				Sequence s1 = F.m_Sequences.get(i);
				Sequence s2 = F.m_Sequences.get(j);
				boolean isS1FirstItemSmallest = s1.isFirstItemSmallestMIS( misTable );
				boolean isS2LastItemSmallest = s2.isLastItemSmallestMIS( misTable );
				
				
				if(isS1FirstItemSmallest && misTable.getMIS( s2.getLastItem() ) > misTable.getMIS( s1.getFirstItem() ) && s1.isEqual(s2,1,s2.getLength()-1)){
//					if(misTable.getMIS( s2.getLastItem() ) > misTable.getMIS( s1.getFirstItem() ) && s1.isEqual(s2,1,s2.getLength()-1)){
						int l = s2.getLastItem();
						if(s2.m_ItemSets.get(s2.m_ItemSets.size()-1).getLength()==1){
							Sequence c1 = s1.getClone();
							ItemSet lItemSet = new ItemSet( misTable );
							lItemSet.addItem(l);
							c1.addItemSetLast(lItemSet);
							newFS.AddSequenceWithoutDup(c1);

							if(s1.getLength()==2 && s1.getSize()==2 && misTable.getMIS( s2.getLastItem() ) > misTable.getMIS( s1.getLastItem() ) ){
								Sequence c2 = s1.getClone();
								c2.m_ItemSets.get(c2.m_ItemSets.size()-1).addItem(l);
								newFS.AddSequenceWithoutDup(c2);
							}
						}else{
							if((s1.getLength()==2 && s1.getSize()==1 && misTable.getMIS( s2.getLastItem() ) > misTable.getMIS( s1.getLastItem() )) || s1.getLength()>2){
								Sequence c2 = s1.getClone();
								c2.m_ItemSets.get(c2.m_ItemSets.size()-1).addItem(l);
								newFS.AddSequenceWithoutDup(c2);
							}
						}
					//}
				}else
				{
					if(isS2LastItemSmallest && misTable.getMIS( s1.getFirstItem() ) > misTable.getMIS( s2.getLastItem() ) && s2.isEqual(s1,s2.getLength()-2,0)){
						/////////reverse
//						if(misTable.getMIS( s1.getFirstItem() ) > misTable.getMIS( s2.getLastItem() ) && s2.isEqual(s1,s2.getLength()-2,0)){
							int l = s1.getFirstItem();
							if(s1.m_ItemSets.get(0).getLength()==1){
								Sequence c1 = s2.getClone();
								ItemSet lItemSet = new ItemSet( misTable );
								lItemSet.addItem(l);
								c1.addItemSetFirst(lItemSet);
								newFS.AddSequenceWithoutDup(c1);

								if(s2.getLength()==2 && s2.getSize()==2 && misTable.getMIS( s1.getFirstItem() ) > misTable.getMIS( s2.getFirstItem() ) ){
									Sequence c2 = s2.getClone();
									c2.m_ItemSets.get(0).addItem(l);
									newFS.AddSequenceWithoutDup(c2);
								}
							}else{
								if((s2.getLength()==2 && s2.getSize()==1 && misTable.getMIS( s1.getFirstItem() ) > misTable.getMIS( s2.getFirstItem() )) || s2.getLength()>2){
									Sequence c2 = s2.getClone();
									c2.m_ItemSets.get(0).addItem(l);
									newFS.AddSequenceWithoutDup(c2);
								}
							}
						//}
					
					
					
					}else{
						if(s1.isEqual(s2,0,s2.getLength()-1))
						{
							Sequence c1 = s1.getClone();
							if(s2.m_ItemSets.get(s2.m_ItemSets.size()-1).getLength()==1){
								ItemSet newItemSet = new ItemSet( misTable );
								newItemSet.addItem(s2.getLastItem());
								c1.addItemSetLast(newItemSet);
								newFS.AddSequenceWithoutDup(c1);
							}else
							{
								c1.m_ItemSets.get(c1.m_ItemSets.size()-1).addItem(s2.getLastItem());
								newFS.AddSequenceWithoutDup(c1);
							}
						}
						
					}
				}

				
			}
		}
		return newFS;
	}
}


class Parameters {

	public MisTable m_MisTable = null;
	float SDC = 0.0f;
	
	public Parameters(){
		m_MisTable = new MisTable();
	}

	public void setSDC(float SDCTh){
		SDC = SDCTh;
	}

}





class Sequence {
	public List <Algorithm.ItemSet> m_ItemSets;
	public int m_iCount = 0;
	
	public Sequence(){
		m_ItemSets= new ArrayList<ItemSet>();
	}
	
	public Sequence( List<ItemSet> is ){
		m_ItemSets = is;
	}
	
	public void addItemSetFirst(ItemSet is){
		m_ItemSets.add(0, is); 
	}

	public void addItemSetLast(ItemSet is){
		m_ItemSets.add(is);
	}
	
	public int getFirstItem(){
		return m_ItemSets.get(0).GetItemAtIndex(0);
	}

	
	
	public static Sequence CreateSequenceFromString( String str, MisTable misTable )
	{
		String[][] tagPairs = {{"{","}"}};
		
		List<String> itemSetsStr = MiscelaniousFunctions.BlockExtractor(str,tagPairs,true);
		Sequence seq = new Sequence();
		for(String itemSetStr:itemSetsStr){
			List<String> idStrs = MiscelaniousFunctions.StringParse(itemSetStr, ",", true);
			LinkedList<Integer> itemIds = new LinkedList<Integer>();
			for( int i = 0; i < idStrs.size(); i++ )
			{
				itemIds.add( Integer.parseInt( idStrs.get(i) ) );
			}
			ItemSet itemSet = new ItemSet( itemIds, misTable );
			seq.addItemSetLast(itemSet);
		}

		return seq;
	}
	
	
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( '<' );
		for( int i = 0; i < m_ItemSets.size(); i++ )
		{
			sb.append( m_ItemSets.get(i).toString() );
		}
		
		sb.append( ">  Count:  " + m_iCount );
		
		return sb.toString();
	}

	
	
	// Gets the size of this sequence
	public int GetSize()
	{
		return m_ItemSets.size();
	}



	// Returns true if this sequence contains the given candidate
	public boolean ContainsSequence( Sequence c )
	{
		
		for( int i = 0; i < m_ItemSets.size() - c.m_ItemSets.size() + 1; i++ )
		{
			// Start searching if this contains the start of the sequence c
			if( m_ItemSets.get( i ).ContainsItems( c.m_ItemSets.get( 0 ) ) == ItemSet.ContainsRes.Cr_Yes )
			{
				if( c.m_ItemSets.size() <= 1 )
				{
					// Found the whole sequence
					return true;
				}
				
				// Check for the rest of the sequence
				int iSubIndex = 1;
				int iMainIndex = i + 1;
				while( iMainIndex < m_ItemSets.size() )
				{
					if( m_ItemSets.get(iMainIndex).ContainsItems( c.m_ItemSets.get( iSubIndex ) ) == ItemSet.ContainsRes.Cr_Yes )
					{
						// Found the next component
						iSubIndex++;
						if( iSubIndex >= c.m_ItemSets.size() )
						{
							// Found the whole sequence
							return true;
						}
					}
					iMainIndex++;
				}
			}
		}
		
		return false;
	}

	
	
	// Returns true if this sequence contains an item
	public boolean ContainsItem( int iItemId )
	{
		for( ItemSet is : m_ItemSets )
		{
			if( is.ContainsItem( iItemId ) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public double getMinMaxSupprtDiff( Map<Integer,Integer> supportCount, int iNumSequences ){
		double minS = Double.POSITIVE_INFINITY;
		double maxS = Double.NEGATIVE_INFINITY;
		for(int i=0 ; i<m_ItemSets.size() ; i++){
			ArrayList<Integer> itemsList = new ArrayList<Integer>();
			itemsList.addAll(m_ItemSets.get(i).GetItems());
			for(int j=0 ;j<itemsList.size();j++){
				double num = supportCount.get(itemsList.get(j));
				if( num < minS)
				{
					minS = num;
				}
				if( num > maxS)
				{
					maxS = num;
				}
			}
		}

		return Math.abs(maxS-minS)/(double)iNumSequences;
		
	}
	
	
	// Gets the minimum MIS item
	public int GetMinMisItem( int[] itemMisOrder )
	{
		for( int iOrderedItemId : itemMisOrder )
		{
			for( ItemSet itemSet : m_ItemSets )
			{
				if( itemSet.ContainsItem( iOrderedItemId ) )
				{
					return iOrderedItemId;
				}
			}
		}
		
		return -1;
	}

	
	
	public enum CreateWithoutMinMisMethod
	{
		Cwm_SingleRemove_MultipleSequences,
		Cwm_RemoveFirstOccuranceOnly,
		Cwm_RemoveAllOccurances,
	}
	
	// Create a list of sequences that are made of
	// this without an occurrence of the min MIS item
	public List<Sequence> CreateSequencesWithoutMinMisItem(int[] itemMisOrder, CreateWithoutMinMisMethod method)
	{
		List<Sequence> seqs = new ArrayList<Sequence>();
		int iMinMisItem = GetMinMisItem( itemMisOrder );
		
		if( method != CreateWithoutMinMisMethod.Cwm_SingleRemove_MultipleSequences )
		{
			// Make a new sequence for each removal
			for( int i = 0; i < m_ItemSets.size(); i++ )
			{
				if( m_ItemSets.get( i ).ContainsItem( iMinMisItem ) )
				{
					// Create a sequence with this item set having a removed item
					Sequence s = new Sequence();
					for( int j = 0; j < m_ItemSets.size(); j++ )
					{
						ItemSet newSet = m_ItemSets.get( j ).Copy();
						if( i == j )
						{
							newSet.RemoveItemId( iMinMisItem );
						}
						if( !newSet.IsEmpty() )
						{
							s.addItemSetLast( newSet );
						}
					}
				}
			}
		}
		else
		{
			// Make only one modified sequence
			Sequence s = getClone();
			for( int j = 0; j < s.m_ItemSets.size(); j++ )
			{
				ItemSet is = s.m_ItemSets.get(j);
				if( is.ContainsItem( iMinMisItem ) )
				{
					is.RemoveItemId( iMinMisItem );
					if( !is.IsEmpty() )
					{
						s.m_ItemSets.remove( j-- );
					}
					
					if( method == CreateWithoutMinMisMethod.Cwm_RemoveFirstOccuranceOnly )
					{
						break;
					}
				}

			}
			
			seqs.add( s );
		}
			
		
		return seqs;
	}
	
	
	
	public int getExplicitMinMIS( MisTable misTable ){
		int minCount=0;
		double minMIS = Double.POSITIVE_INFINITY;
		int minIndex = -1;
		int k = 0;
		for(int i=0 ; i<m_ItemSets.size() ; i++){
			ArrayList<Integer> itemsList = new ArrayList<Integer>();
			itemsList.addAll(m_ItemSets.get(i).GetItems());
			for(int j=0 ;j<itemsList.size();j++){
				if( misTable.getMIS( itemsList.get(j) ) < minMIS)
				{
					minMIS = misTable.getMIS( itemsList.get(j) );
					minCount = 1;
					minIndex = k;
				}
				else
				{
					if( misTable.getMIS( itemsList.get(j) ) == minMIS )
					{
						minCount = minCount + 1;
					}
				}
				k++;
			}
		}

		if(minCount==1)
			return minIndex;
		else
			return -1;

	}	
	

	public double getMinMIS( MisTable misTable ){
		double minMIS = Double.POSITIVE_INFINITY;
		for(int i=0 ; i<m_ItemSets.size() ; i++){
			ArrayList<Integer> itemsList = new ArrayList<Integer>();
			itemsList.addAll(m_ItemSets.get(i).GetItems());
			for(int j=0 ;j<itemsList.size();j++){
				if( misTable.getMIS( itemsList.get(j) ) < minMIS)
				{
					minMIS = misTable.getMIS( itemsList.get(j) );
				}
			}
		}

		return minMIS;
	}	


	public boolean isEqual(Sequence seq){
		if(this.getSize() != seq.getSize() || this.getLength() != seq.getLength())
			return false;
		
		Iterator<ItemSet> s1Iter = this.m_ItemSets.iterator(); 
		Iterator<ItemSet> s2Iter = seq.m_ItemSets.iterator();
		
		while(s1Iter.hasNext()){
			ItemSet s1ItemSet = s1Iter.next();
			ItemSet s2ItemSet = s2Iter.next();
			if(!s1ItemSet.equals(s2ItemSet))
				return false;
		}
		return true;
	}
	
	
	
	// isEquals two sequences with an ignore index option
	public boolean isEqual( Sequence s2, int iIgnore1, int iIgnore2 )
	{

		if (this.getLength() != s2.getLength())
		{
			return false;
		}
		
		Sequence newS1 = this.getClone();
		Sequence newS2 = s2.getClone();
		
		newS1.RemoveItemByIndex(iIgnore1);
		newS2.RemoveItemByIndex(iIgnore2);
		
		return newS1.isEqual(newS2);
		
	}
	
	
	
	public int getLength(){
		int sum = 0;
		for(ItemSet is : m_ItemSets){
			sum = sum + is.getLength();
		}
		return sum;
	}
	
	public int getSize(){
		return m_ItemSets.size();
	}
	


	
	public int getLastItem(){
		return m_ItemSets.get(m_ItemSets.size()-1).last();
	}
	
	public boolean isFirstItemSmallestMIS( MisTable misTable ){
		int minIndex = getExplicitMinMIS(misTable);
		if(minIndex==0)
			return true;
		return false;
		
	}
	
	public boolean isLastItemSmallestMIS( MisTable misTable ){
		int minIndex = getExplicitMinMIS(misTable);
		if(minIndex==getLength()-1)
			return true;
		return false;
	}
	
	
	
	public Sequence getClone(){
		ArrayList<ItemSet> newItemSets = new ArrayList<ItemSet>();
		for(ItemSet it : m_ItemSets){
			newItemSets.add(it.Copy() );
		}

		Sequence newSeq = new Sequence(newItemSets);
		return newSeq;
	}
	
	
	
	// Removes an item from the sequence by absolute index
	public void RemoveItemByIndex( int iIndex )
	{
		int iBlockIndex = 0;
		for(ItemSet it : m_ItemSets)
		{
			int iItemSetLen = it.getLength();
			if( iIndex < iBlockIndex + iItemSetLen )
			{
				it.RemoveItemByIndex( iIndex - iBlockIndex );
				break;
			}
			else
			{
				iBlockIndex += iItemSetLen;
			}
		}
		
		RemoveEmptyItemSets();
	}
	
	
	
	// Removes any empty item sets from the sequence
	public void RemoveEmptyItemSets()
	{
		for( int i = 0; i < m_ItemSets.size(); i++ )
		{
			if( m_ItemSets.get( i ).getLength() == 0 )
			{
				m_ItemSets.remove( i-- );
			}
		}
	}

}


class MisTable {

	protected Map<Integer, Float> m_Table = new HashMap<Integer, Float>();
	
	public float getMIS(Integer key){
		return m_Table.get(key);
	}

	public void addMIS(int key, float MISValue){
		m_Table.put(key, MISValue);
	}
}



class Pair<First, Second> {
    private First my_First;
    private Second my_Second;

    public Pair(First first, Second second) {
        my_First = first;
        my_Second = second;
    }

    public void SetFirst(First first) {
        my_First = first;
    }

    public void SetSecond(Second second) {
        my_Second = second;
    }

    public First GetFirst() {
        return my_First;
    }

    public Second GetSecond() {
        return my_Second;
    }
}






class MiscelaniousFunctions
{

	public static List<String> StringParse( String str, char separater, boolean bTrim )
	{
		List<String> v = new ArrayList<String>();
		
		int iStartIndex = 0;
		while( (iStartIndex != -1) && (iStartIndex < str.length() ))
		{			
			int iEndIndex = -1;
			if( iEndIndex + 1 < str.length() )
			{
				iEndIndex = str.indexOf( separater, iStartIndex );
			}
			if( iEndIndex == -1 )
			{
				iEndIndex = str.length();
			}
			
			
			String s = "";
			if( iStartIndex != iEndIndex )
			{
				s = str.substring( iStartIndex, iEndIndex );
				v.add( bTrim ? s.trim() : s );
			}
			
			iStartIndex = iEndIndex + 1;
		}
		
		return v;
	}
	
	
	
	public static List<String> StringParse( String str, String separater, boolean bTrim )
	{
		List<String> v = new ArrayList<String>();
		
		int iStartIndex = 0;
		while( (iStartIndex != -1) && (iStartIndex < str.length() ))
		{			
			int iEndIndex = -1;
			if( iEndIndex + 1 < str.length() )
			{
				iEndIndex = str.indexOf( separater, iStartIndex );
			}
			if( iEndIndex == -1 )
			{
				iEndIndex = str.length();
			}
			
			
			String s = "";
			if( iStartIndex != iEndIndex )
			{
				s = str.substring( iStartIndex, iEndIndex );
				v.add( bTrim ? s.trim() : s );
			}
			
			iStartIndex = iEndIndex + separater.length();
		}
		
		return v;
	}
	
	
	
	// Parses a string given a separator character
	public static List<String> ParseString( String str, char separater )
	{
		return StringParse( str, separater, false );
	}
	
	
	

	public static String LoadFileAsString( String path )
	{
		FileInputStream inputStream = null;
		InputStreamReader streamReader = null;
		BufferedReader bufferedReader = null;

		StringBuilder fileText = new StringBuilder();
		
		try
		{
			inputStream = new FileInputStream( path );
			streamReader = new InputStreamReader( inputStream, "UTF8" );
			bufferedReader = new BufferedReader( streamReader );

			String line = "";
			while( (line = bufferedReader.readLine()) != null )
			{
				if( fileText.length() > 0 )
				{
					fileText.append( "\n" + line );	
				}
				else
				{
					fileText.append( line );
				}
			}
		}
		catch( Exception e )
		{
			fileText = null;
		}
		
		if( bufferedReader != null )	{ try { bufferedReader.close(); } catch (IOException e) {} }
		if( streamReader != null )		{ try { streamReader.close();	} catch (IOException e) {} }
		if( inputStream != null )		{ try { inputStream.close();	} catch (IOException e) {} }
	
		return fileText == null ? "" : fileText.toString();
	}


	public static boolean SaveFileAsString(String text, String path)
	{
		boolean bSuccess = false;
		PrintWriter out = null;
	
		try
		{
			out = new PrintWriter( path );
			out.println( text );
			bSuccess = true;
		}
		catch( Exception e )
		{
		}
		
		if( out != null )	{ out.close(); }
		
		return bSuccess;
	}


	
	public static List<String> BlockExtractor( String text, String[][] tagPairs )	{ return BlockExtractor( text, tagPairs, true ); }
	public static List<String> BlockExtractor( String text, String[][] tagPairs, boolean bRecursive )
	{
		List<String> retStrs = new ArrayList<String>();
		
		int iStartBlock = 0;
		int iIndex = 0;
		while( (iIndex != -1) && (iIndex < text.length()) )
		{
			// Get the index of the next interesting tag
			int iNextIndex = -1;
			int iPairIndex = -1;
			for( int i = 0; i < tagPairs.length; i++ )
			{
				int j = text.indexOf( tagPairs[i][0], iIndex );
				if( (iNextIndex == -1) || ((j != -1) && (j < iNextIndex)) )	{ iNextIndex = j;	iPairIndex = i; } 
			}
			iIndex = iNextIndex;
			
			
		
			if( iIndex == -1 )
			{
				// Nothing else of interest
				break;
			}
			else
			{
				// May need to skip some text
				// Find the appropriate closing tag
				int iMarkupEndIndex = -1;
				if( !bRecursive )
				{
					iMarkupEndIndex = text.indexOf( tagPairs[iPairIndex][1], iIndex );
					if( iMarkupEndIndex != -1)
					{
						iMarkupEndIndex += tagPairs[iPairIndex][1].length();
					}
				}
				else
				{
					int iDepth = 1;
					iMarkupEndIndex = iIndex + tagPairs[iPairIndex][0].length();

					while( (iDepth > 0) && (iMarkupEndIndex != -1) )
					{
						int iNextOpen = text.indexOf( tagPairs[iPairIndex][0], iMarkupEndIndex );	if( iNextOpen != -1) { iNextOpen += tagPairs[iPairIndex][0].length(); }
						int iNextClose = text.indexOf( tagPairs[iPairIndex][1], iMarkupEndIndex );	if( iNextClose != -1) { iNextClose += tagPairs[iPairIndex][1].length(); }
						
						if( iNextClose == -1 )
						{
							iMarkupEndIndex = -1;
						}
						else
						{
							if( (iNextOpen == -1) || (iNextClose < iNextOpen) )		{ iDepth--;	iMarkupEndIndex = iNextClose; }
							else	{ iDepth++;	iMarkupEndIndex = iNextOpen; }
						}
					}
					
					if( iDepth > 0 )
					{
						// Didn't find a closing tag
						iMarkupEndIndex = -1;
					}
				}
				
				if( iMarkupEndIndex != -1 )
				{
					// Get the block
					if( (iMarkupEndIndex - tagPairs[iPairIndex][1].length()) > (iIndex + tagPairs[iPairIndex][0].length()) )
					{
						String block = text.substring( iIndex + tagPairs[iPairIndex][0].length(), iMarkupEndIndex - tagPairs[iPairIndex][1].length() );
						retStrs.add( block );
					}
					iIndex = iStartBlock = iMarkupEndIndex;
				}
				else
				{
					// No end to this tag, ignore it
					iIndex++;
				}
			}			
		}
		
		return retStrs;
	}
	
}

	



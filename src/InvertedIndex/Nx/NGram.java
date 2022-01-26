package InvertedIndex.Nx;

/// <summary>
/// Models an nGram string which is in essence a string that contains n (n>1) words.
/// 
/// This can be used to model a particular sequence of strings e.g:
/// n = 4, "thank", "you", "very", "much" can be used to stand in for "thank you very much".
/// </summary>
public class NGram
{
	private String[] _Words = null;
	private int _Frequency = 0;
	private String _WordString;

	//region ctors
	/// <summary>
	/// Class constructor
	/// </summary>
	/// <param name="words"></param>
	/// <param name="frequency"></param>
	public NGram(String[] words, int frequency) throws IllegalArgumentException
	{
		_Words = new String[words.length];

		for (int i = 0; i < words.length; i++) // Sanity check this to be sure
		{
			if (words[i] == null || words[i].length() == 0)
				throw new IllegalArgumentException("Gram component cannot be empy or null");

			_Words[i] = words[i];
		}

		_WordString = String.join(" ", _Words); 
		_Frequency = frequency;
	}

	/// <summary>
	/// Hidden class constructor
	/// </summary>
	protected NGram()
	{
		this(null, 0);
	}
	//endregion ctors

	//region props
	/// <summary>
	/// Gets an array strings that make up the ngram.
	/// </summary>
	public String[] getWords() { return _Words; }

	/// <summary>
	/// Gets the frequency with which this string occurs.
	/// </summary>
	public int getFrequency() { return _Frequency; }

	/// <summary>
	/// Gets a strings sequence of the <see cref="Words"/>
	/// collection seperated with space characters.
	/// </summary>
	public String getWordsString() { return _WordString; }
	//endregion props

	//region methods
	/// <summary>
	/// Get an NGram from an array of words starting at <paramref name="iStartIdx"/> positiion.
	/// 
	/// This will work only if bounds of <paramref name="words"/> array have been checked before
	/// calling this.
	/// </summary>
	/// <param name="gramLen"></param>
	/// <param name="words"></param>
	/// <param name="iStartIdx"></param>
	/// <returns></returns>
	public static String[] NGramFromStringArray(int gramLen,
												String[] words,
												int iStartIdx)
	{
		String[] word = new String[gramLen];

		for (int j = iStartIdx, idx = 0; idx < gramLen; j++, idx++)
			word[idx] = words[j].toLowerCase();

		return word;
	}

	/// <summary>
	/// Increments the frequency of this nGram by one.
	/// </summary>
	/// <returns></returns>
	public int IncrementFrequency()
	{
		_Frequency += 1;

		return _Frequency;
	}

	@Override
	public int hashCode()
	{
		return this.getWordsString().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		NGram otherObj = (NGram)obj;
		if (otherObj == null)
			return false;
			
		return (this.getWordsString().compareToIgnoreCase(otherObj.getWordsString()) == 0);
	}

	@Override
	public String toString()
	{
		return this.getWordsString();
	}

	//endregion methods
}

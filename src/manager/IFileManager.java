package manager;


public interface IFileManager 

{

	//Method to determine whether the file is completed (whether the peer has all the parts of the file)
    public void fileDownloaded();
    
    //Method to determine whether the piece is arrived, given it's index
    public void partDownloaded(int index);
}
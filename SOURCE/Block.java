package noobchain;

import java.util.Date;
import java.util.ArrayList;
public class Block {

	public String hash;
	public String previousHash;
	private String data; //our data will be a simple message.
	public String merkleRoot;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	private long timeStamp; //as number of milliseconds since 1/1/1970.
	private int nonce;
	
	//Block Constructor.
	public Block(String previousHash ) {
		//this.data = data;
		this.previousHash = previousHash;
		this.timeStamp = new Date().getTime();
		this.hash = calculateHash(); //Making sure we do this after we set the other values.
	}
	
	public String calculateHash() {
		String calculatedhash = StringUtil.applySha256( 
				previousHash +
				Long.toString(timeStamp) +
				Integer.toString(nonce) + 
				//data
				merkleRoot
				);
		return calculatedhash;
	}
	
	//Increases nonce value until hash target is reached.
		public void mineBlock(int difficulty) {
			merkleRoot = StringUtil.getMerkleRoot(transactions);
			String target = StringUtil.getDificultyString(difficulty); //Create a string with difficulty * "0" 
			while(!hash.substring( 0, difficulty).equals(target)) {
				nonce ++;
				hash = calculateHash();
			}
			System.out.println("Block Mined!!! : " + hash);
		}
		
		//Add transactions to this block
		public boolean addTransaction(Transaction transaction,Wallet sender,Wallet reciever, float value) {
			//process transaction and check if valid, unless block is genesis block then ignore.
			if(transaction == null) return false;		
			if((previousHash != "0")) {
				if((transaction.processTransaction(sender,reciever, value) != true)) {
					System.out.println("Transaction failed to process. Discarded.");
					return false;
				}
			}
			else {
				transaction.processZKP(sender, reciever);
				if(!transaction.verifyZKP())
					{System.out.println("Verification using ZKP failed!");
					return false;}
				System.out.println("Prime number being used for verification:"+transaction.p);
			}
			transactions.add(transaction);
			System.out.println("Verified Transaction using ZKP...\nTransaction Successfully added to Block");
			return true;
		}
}
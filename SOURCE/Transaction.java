package noobchain;

import java.security.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Transaction {
	
	public String transactionId; // this is also the hash of the transaction.
	public PublicKey sender; // senders address/public key.
	public PublicKey reciepient; // Recipients address/public key.
	public float value;
	public byte[] signature; // this is to prevent anybody else from spending funds in our wallet.
	public String desc; //Comments for a particular txn
	public long g,p,y,h,s;
	public int b;
	public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();
	Random rnd=new Random();  
	private static int sequence = 0; // a rough count of how many transactions have been generated. 
	
	// Constructor: 
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
		this.g=rnd.nextInt(10);
		this.p=GFG.generatePrime();
	}
	
	public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs, String desc) {
		this.sender = from;
		this.reciepient = to;
		this.value = value;
		this.inputs = inputs;
		this.desc = desc;
		this.g=rnd.nextInt(10);
		this.p=GFG.generatePrime();
	}
	
	// This Calculates the transaction hash (which will be used as its Id)
	private String calulateHash() {
		sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
		return StringUtil.applySha256(
				StringUtil.getStringFromKey(sender) +
				StringUtil.getStringFromKey(reciepient) +
				Float.toString(value) + sequence
				);
	}
	
	//Signs all the data we dont wish to be tampered with.
	public void generateSignature(PrivateKey privateKey) {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		signature = StringUtil.applyECDSASig(privateKey,data);		
	}
	
	//Verifies the data we signed hasnt been tampered with
	public boolean verifiySignature() {
		String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;
		return StringUtil.verifyECDSASig(sender, data, signature);
	}
	
	//Returns true if new transaction could be created.	
	public void processZKP(Wallet sender,Wallet reciever)
	{
		  this.y=sender.generateY(g,p);
		  this.h=sender.generateH(g,p);
		  this.b=reciever.generateB();
		  this.s=sender.generateS(b,p);
	}
	
	public boolean verifyZKP()
	{
		if(power(g,s,p)==h*power(y,b,p))
			return true;
		return false;
	}
	
	public static void addTxnData(Wallet from,Wallet to, float value, String transactionId) {
		final long timeStamp = new Date().getTime();
		from.addTxData(to, timeStamp, value, transactionId);
	}
	
	public void addTxnDatanew(Wallet from,Wallet to, float value) {
		final long timeStamp = new Date().getTime();
		from.addTxData(to, timeStamp, value, transactionId);
	}
	
	public boolean processTransaction(Wallet sender,Wallet reciever,float value) {
			
			processZKP(sender,reciever);
			if(verifyZKP()==false) {
				System.out.println(" Zero-Knowledge-Proof test failed !");
				return false;
			}  
			System.out.println("Prime number being used for verification:"+this.p);
			
			if(verifiySignature() == false) {
				System.out.println("#Transaction failed to verify");
				return false;
			}
			//gather transaction inputs (Make sure they are unspent):
			for(TransactionInput i : inputs) {
				i.UTXO = NoobChain.UTXOs.get(i.transactionOutputId);
			}

			//check if transaction is valid:
			if(getInputsValue() < NoobChain.minimumTransaction) {
				System.out.println("#Transaction Inputs to small: " + getInputsValue());
				return false;
			}
			
			//generate transaction outputs:
			float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
			transactionId = calulateHash();
			outputs.add(new TransactionOutput( this.reciepient, value,transactionId)); //send value to recipient
			outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender		
					
			//add outputs to Unspent list
			for(TransactionOutput o : outputs) {
				NoobChain.UTXOs.put(o.id , o);
			}
			
			//remove transaction inputs from UTXO lists as spent:
			for(TransactionInput i : inputs) {
				if(i.UTXO == null) continue; //if Transaction can't be found skip it 
				NoobChain.UTXOs.remove(i.UTXO.id);
			}
			
			addTxnData(sender, reciever, value, transactionId);
			return true;
		}
		
	//returns sum of inputs(UTXOs) values
		public float getInputsValue() {
			float total = 0;
			for(TransactionInput i : inputs) {
				if(i.UTXO == null) continue; //if Transaction can't be found skip it 
				total += i.UTXO.value;
			}
			return total;
		}

	//returns sum of outputs:
		public float getOutputsValue() {
			float total = 0;
			for(TransactionOutput o : outputs) {
				total += o.value;
			}
			return total;
		}
		
        static	long power(long x, long y, long p) { 
			long  res = 1; // Initialize result 
			
			//Update x if it is more than or 
			// equal to p 
			x = x % p; 

			while (y > 0) { 
				if ((y & 1) == 1) 
					{long temp=res*x;
					res = (temp) % p;} 
			
				// y must be even now 
				y = y /2; // y = y/2 
				x = (x * x);
				x=x%p;
			} 
			
			return res; 
		}
}
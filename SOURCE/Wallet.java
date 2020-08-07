package noobchain;

import java.security.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

public class Wallet {
	
	public PrivateKey privateKey;
	public PublicKey publicKey;
	private long x;
	private long r;
	Random rnd=new Random();
	
	public long generateY(long g,long p){
		return power(g,x,p);
	}
	
	public long generateH(long g,long p){
		this.r=2+rnd.nextInt(1000);
		return power(g,r,p);
	}
	
	public int generateB(){
		return (rnd.nextInt(120))%2;
	}
	
	public long generateS(int b,long p){
		return (r+b*x)%p;
	}
	
	public String Owner;//Owner of wallet
	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //only UTXOs owned by this wallet.
	public HashMap<String, ArrayList<String>> fromTo = new HashMap< String, ArrayList<String>>(); // <txnID, <from, to>>
	
	public Wallet(){
		generateKeyPair();	
	}
	
	public Wallet(String Owner){
		generateKeyPair();	
		this.Owner=Owner;
	}
	
	public String getOwner() {
		return Owner;
	}
	
	public void generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
	        	KeyPair keyPair = keyGen.generateKeyPair();
	        	// Set the public and private keys from the keyPair
	        	privateKey = keyPair.getPrivate();
	        	publicKey = keyPair.getPublic();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void addTxData(Wallet to, long timeStamp, float value, String transactionId) {
		fromTo.put(transactionId, new ArrayList<String>(Arrays.asList(this.getOwner(), to.getOwner(), Float.toString(value))));
		to.fromTo.put(transactionId, new ArrayList<String>(Arrays.asList(this.getOwner(), to.getOwner(), Float.toString(value))));
	}
	
	public void txnList() {
		System.out.println("\nThe details of all transactions done with this wallet owned by "+ this.getOwner()+" are as follows\nTxnID								 | FROM		TO	Amount of goods transferred(Rupees)" );
		for (Entry<String, ArrayList<String>> entry : fromTo.entrySet()) {
	        System.out.print(entry.getKey()+" | ");
	        for(String No : entry.getValue()){
	            System.out.print(No+" ");
	        }
	        System.out.println();
	    }
	}
	
	//returns balance and stores the UTXO's owned by this wallet in this.UTXOs
		public float getBalance() {
			float total = 0;	
	        for (Map.Entry<String, TransactionOutput> item: NoobChain.UTXOs.entrySet()){
	        	TransactionOutput UTXO = item.getValue();
	            if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
	            	UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
	            	total += UTXO.value ; 
	            }
	        }  
			return total;
		}
		
		//Generates and returns a new transaction from this wallet.
		public Transaction sendFunds(PublicKey _recipient,float value ) {
			//fromTo.put(StringUtil.applySha256(Long.toString(Date().getTime())), new ArrayList<String>(Arrays.asList(this.Owner(), _recipient.)));
			if(getBalance() < value) { //gather balance and check funds.
				System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
				return null;
			}
	    //create array list of inputs
			ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
	    
			float total = 0;
			for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
				TransactionOutput UTXO = item.getValue();
				total += UTXO.value;
				inputs.add(new TransactionInput(UTXO.id));
				if(total > value) break;
			}
			
			Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
			newTransaction.generateSignature(privateKey);
			
			for(TransactionInput input: inputs){
				UTXOs.remove(input.transactionOutputId);
			}
			return newTransaction;
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
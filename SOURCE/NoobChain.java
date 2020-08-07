package noobchain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.String;

public class NoobChain {
	
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>(); //list of all unspent transactions.
	public static int difficulty = 5;
	public static float minimumTransaction = 0.1f;
	public static Wallet farmer[]=new Wallet[10];
	public static Wallet transport[]=new Wallet[10];
	public static Wallet supermarket[]=new Wallet[10];
	public static Transaction Nature[] = new Transaction[10];
	public static void main(String[] args) {	
		
				//Setup Bouncey castle as a Security Provider
				Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); 
				Wallet coinbase = new Wallet("Genesis");
				System.out.println("Creating and Mining Genesis block... ");
				Block genesis = new Block("0");
				addBlock(genesis);
				System.out.println("");				
				Block blockchainz[] = new Block[10];
				for(int i=0;i<10;i++) {
					System.out.println("\nCreating and Mining "+i+"th block... ");
					if(i==0) {
						blockchainz[0] = new Block(genesis.hash);
						addBlock(blockchainz[0]);
					}
					else {
						blockchainz[i] = new Block(blockchainz[i-1].hash);
						addBlock(blockchainz[i]);
					}
					System.out.println("\nCreating wallet of "+i+"th farmer... ");
					farmer[i] = new Wallet("Farmer "+i);
					System.out.println("Wallet of "+i+"th farmer Created... ");
					
					System.out.println("Creating genesis transaction, which sends "+ (10000f + 20*i) +" NoobCoin worth of goods to farmer["+i+"], i.e., farmer "+i+"");
					Nature[i] = new Transaction(coinbase.publicKey, farmer[i].publicKey, 10000f + 20*i, null); //farmer extracts materials worth 100 + 20*i from nature
					Nature[i].transactionId = StringUtil.applySha256(String.valueOf(i));
					Nature[i].addTxnDatanew(coinbase, farmer[i], 10000f+ 20*i);
					System.out.println(10000f+20*i +" NoobCoin worth of goods generated in farmer["+i+"], i.e., farmer "+i+"");
					Nature[i].generateSignature(coinbase.privateKey);
					Nature[i].outputs.add(new TransactionOutput(Nature[i].reciepient, Nature[i].value, Nature[i].transactionId));
					NoobChain.UTXOs.put(Nature[i].outputs.get(0).id, Nature[i].outputs.get(0));
					genesis.addTransaction(Nature[i],coinbase,farmer[i],10000f+20*i);
					
					System.out.println("\nCreating wallet of "+i+"th transporter... ");
					transport[i] = new Wallet("transport "+i);
					System.out.println("Wallet of "+i+"th transporter Created... ");
					System.out.println("Initiating Transaction:");
					blockchainz[i].addTransaction(farmer[i].sendFunds(transport[i].publicKey, 60*i+30),farmer[i],transport[i], 60*i +30);
					System.out.println(60*i+30 +" NoobCoin worth of goods sent from farmer "+i+" to transporter "+i+"'s in wallet, transport["+i+"]");
					
					System.out.println("\nCreating wallet of "+i+"th supermarket retailer... ");
					supermarket[i] = new Wallet("Supermarket "+i);
					System.out.println("Wallet of "+i+"th supermarket retailer Created... ");
					System.out.println("Initiating Transaction:");
					blockchainz[i].addTransaction(transport[i].sendFunds(supermarket[i].publicKey, 20*i+30),transport[i],supermarket[i], 20*i +30);
					System.out.println(20*i+30 +" NoobCoin worth of goods sent from transporter "+i+" to supermarket retailer "+i+"'s wallet, supermarket["+i+"]\n");
					
					System.out.println(farmer[i].getOwner()+"'s balance is: " + farmer[i].getBalance());
					System.out.println(transport[i].getOwner()+"'s balance is: " + transport[i].getBalance());
					System.out.println(supermarket[i].getOwner()+"'s balance is: " + supermarket[i].getBalance()+"\n");
				}
				
		viewUser(farmer[9]);
		viewUser(transport[9]);
				
		System.out.print("\nVerifying Validity of this blockchain: ");
		isChainValid();
	}
	
	public static void viewUser(Wallet w) {
		w.txnList();
	}
	
	public static void viewUser() {
		for(int i=0;i<10;i++) {
			System.out.println(farmer[i].getOwner()+" holding goods worth "+farmer[i].getBalance()+", "+ transport[i].getOwner() +" holding goods worth "+transport[i].getBalance()+", "+ supermarket[i].getOwner()+" holding goods worth "+supermarket[i].getBalance() );
		}
	}
	
	public static Boolean isChainValid() {
		Block currentBlock; 
		Block previousBlock;
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		for(int i=0;i<10;i++) {
			tempUTXOs.put(Nature[i].outputs.get(0).id,Nature[i].outputs.get(0));	
		}
		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {
			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}
			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);
				
				if(!currentTransaction.verifiySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false; 
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false; 
				}
				
				for(TransactionInput input: currentTransaction.inputs) {	
					tempOutput = tempUTXOs.get(input.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}
					
					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}
					
					tempUTXOs.remove(input.transactionOutputId);
				}
				
				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}
				
				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain is valid");
		return true;
	}
	
	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
}
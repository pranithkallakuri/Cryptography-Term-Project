package noobchain;

//Miller-Rabin primality test 
//import java.io.*; 
import java.util.Random;
//import java.math.*; 

class GFG { 

	static long power(long x, long y, long p) { 
		
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
	
	static boolean millerTest(long d, long n) { 
		
		Random rnd=new Random();
		int a = 2+rnd.nextInt(15); 
		long x = power(a, d, n); 
	
		if (x == 1 || x == n- 1) 
			return true; 
	
		while (d != n - 1) { 
			x = (x * x) ;
			x=x% n; 
			d *= 2; 
		
			//if (x == 1) 
				//return false; 
			if (x==1 || x == n- 1) 
				return true; 
		} 
	
		// Return composite 
		return false; 
	} 
	 
	static boolean isPrime(long n, int k) { 
		
		// Corner cases 
		if (n <= 1 || n == 4) 
			return false; 
		if (n <= 3) 
			return true; 
	
		 
		long d = n - 1; 
		
		while (d % 2 == 0) 
			d /= 2; 
	
		for (int i = 0; i < k; i++) 
			if (!millerTest(d, n)) 
				return false; 
	
		return true; 
	} 
	public static boolean normalPrime(int n)
	{
		for(int i=2;i*i<=n;i++)
		{
			if(n%i==0)
				return false;
		}
		return true;
	}
	
	public static long generatePrime() { 
		
			Random random = new Random();
			long prime,flag=0;
			for(int i=0;;i++)
			{
				int value=1000000+random.nextInt(100000000);
				if(GFG.isPrime(value,3))
					{	prime=value;
						flag=1;
					 	return prime; 
					}
			} 
	} 
} 


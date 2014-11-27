import java.util.*;

class Tokenizer {
	private final String rawStr;
	private int pos;

	private static final int OPEN_BRACKET = 1;
	private static final int CLOSE_BRACKET = 2;
	private static final int DIGIT = 3;
	private static final int SYMBOL = 4;

	private static final int UNIQUE_TYPE = 1000;
	
	private int getType(char c){
		if(Character.isDigit(c)){
			return DIGIT;
		} else if(c == '('){
			return UNIQUE_TYPE;
		} else if(c == ')'){
			return UNIQUE_TYPE;
		} else {
			return SYMBOL;
		}
	}

	public Tokenizer(String str){
		if(str == null){
			rawStr = "";
		} else {
			this.rawStr = str;
		}
		pos = 0;
	}

	public String nextToken(){
		if(pos+1 > rawStr.length())
			return null;

		char lastChar = rawStr.charAt(pos);
		StringBuilder sb = new StringBuilder();
		sb.append(lastChar);

		//считывание однородной последовательности символов
		while(rawStr.length() > (pos+1)){
			int lastCharType = getType(lastChar);
			int nextCharType = getType(rawStr.charAt(pos+1));
			if(lastCharType == UNIQUE_TYPE || nextCharType == UNIQUE_TYPE || lastCharType != nextCharType)
				break;
			sb.append(rawStr.charAt(pos+1));
			pos++;
		}
		pos++;
		return sb.toString();
	}

	public String[] allTokens(){
		List<String> tokens = new ArrayList<String>();
		String token;
		while((token = nextToken()) != null){
			tokens.add(token);
		}
		return tokens.toArray(new String[tokens.size()]);
	}
}

class Calc {
	private String[] data;

	public Calc(String str){
		this.data = new Tokenizer(str).allTokens();
	}

	public int eval(){
		Stack<String> stack = toPolish(data);
		System.out.println("stack = " + stack);
		return recEval(stack);
	}

	public int recEval(Stack<String> stack){
		if(isOp(stack.peek())){
			String op = stack.pop();
			int arg1 = recEval(stack);
			int arg2 = recEval(stack);
			return apply(op, arg2, arg1);	//внимание на порядок аргументов!
		} else {
			return Integer.parseInt(stack.pop());
		}
	}


	private boolean isOp(String str){
		return new ArrayList(){{
			add("+"); 
			add("-"); 
			add("*"); 
			add("/"); 
		}}.contains(str);
	}

	interface F<A,B,R>{
		R apply(A a, B b);
	}
	

	private int apply(String op, int arg1, int arg2){
		HashMap<String, F<Integer,Integer,Integer>> map = new HashMap(){{
			put("+", new F<Integer,Integer,Integer>(){
				public Integer apply(Integer a, Integer b){
					return a+b;
				}
			});
			put("-", new F<Integer,Integer,Integer>(){
				public Integer apply(Integer a, Integer b){
					return a-b;
				}
			});
			put("*", new F<Integer,Integer,Integer>(){
				public Integer apply(Integer a, Integer b){
					return a*b;
				}
			});
			put("/", new F<Integer,Integer,Integer>(){
				public Integer apply(Integer a, Integer b){
					return a/b;
				}
			});
		}};
		return map.get(op).apply(arg1, arg2);
	}


	private int priority(String op){
		return new HashMap<String, Integer>(){{
			put("+", 5);
			put("-", 5);
			put("*", 9);
			put("/", 9);
			put("(", 1);
		}}.get(op);
	}

	public boolean isNumber(String str){
		for(int i = 0; i < str.length(); i++){
			if(!Character.isDigit(str.charAt(i)))
				return false;
		}
		return true;
	}

	public Stack<String> toPolish(String[] tokens){
		Stack<String>  operands = new Stack<String>();
		Stack<String>  outStack = new Stack<String>();
		
		for(String token : tokens){
			if(isNumber(token)){
				outStack.push(token);
			} else if("(".equals(token)){
				operands.push(token);
			} else if(")".equals(token)){
				while(!operands.isEmpty() && !"(".equals(operands.peek())){
					outStack.push(operands.pop());
				}
				operands.pop();
			} else if(operands.isEmpty()){
				operands.push(token);
			} else {
				if(priority(token) >= priority(operands.peek())){
					operands.push(token);
				} else {
					while(!operands.isEmpty() && priority(token) < priority(operands.peek())){
						outStack.push( operands.pop() );
					}
					operands.push(token);
				}
			}
		}
		while(!operands.isEmpty()){
			outStack.push(operands.pop());
		}
		return outStack;
	}
}

public class Calculator{ 
	public static void main(String args[]){
		System.out.println(new Calc("((7-5)*2-1*1*(1+1)/2)*4/2").eval());
	}
}

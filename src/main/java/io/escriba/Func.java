package io.escriba;

import io.escriba.functional.T2;
import io.escriba.functional.T3;

public abstract class Func {

	public static <A, B> T2<A, B> t2(A a, B b) {
		return new T2(a, b);
	}

	public static <A, B, C> T3<A, B, C> t3(A a, B b, C c) {
		return new T3(a, b, c);
	}

}

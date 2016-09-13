package io.escriba;

public class T2<A, B> {

	public final A a;
	public final B b;

	public T2(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public static <A, B> T2<A, B> of(A a, B b) {
		return new T2(a, b);
	}

	@Override
	public String toString() {
		return "T2(" + a + ", " + b + ")";
	}
}

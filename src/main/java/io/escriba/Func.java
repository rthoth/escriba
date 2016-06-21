package io.escriba;

public class Func {

	private Func() {
	}

	public static <A, B> T2<A, B> t2(A a, B b) {
		return new T2(a, b);
	}

	public static <A, B, C> T3<A, B, C> t3(A a, B b, C c) {
		return new T3(a, b, c);
	}

	public interface F0<R> {
		R apply() throws Exception;
	}

	public interface F1<R, A> {
		R apply(A a) throws Exception;
	}

	public interface F2<R, A, B> {
		R apply(A a, B b) throws Exception;
	}

	public static class T3<A, B, C> {
		public final A a;
		public final B b;
		public final C c;

		public T3(A a, B b, C c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}

	public static class T2<A, B> {
		public final A a;
		public final B b;

		public T2(A a, B b) {
			this.a = a;
			this.b = b;
		}
	}
}

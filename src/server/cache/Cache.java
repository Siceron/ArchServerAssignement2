package server.cache;

public abstract class Cache {

	protected long n;
	protected long x;
	protected long access;
	protected long byteAccess;
	protected long weight;
	protected double hit;
	protected double byteHit;
	protected boolean warmup;
	
	public Cache(long n, long x) {
		super();
		this.n = n;
		this.x = x;
		this.access = 0;
		this.byteAccess = 0;
		this.weight = 0;
		this.hit = 0;
		this.byteHit = 0;
		this.warmup = false;
	}
}

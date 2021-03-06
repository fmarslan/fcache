/*******************************************************************************
 *
 *
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016  FMA
 *
 * Fatih Mehmet ARSLAN
 * fmarslan06@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package com.fma.common.cache;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("serial")
public class CacheObject<K, V> implements Serializable {

	public interface CacheListener<V> {
		public V getData();
	}

	public interface CacheDependecy extends Serializable {
		Boolean isExpired();

		public void restart();
	}

	public static class TimeDependency implements CacheDependecy {

		public enum TimeDependencyType {
			SECOND, MINUTE, HOUR, DAY, WEEK, MONDAY
		}

		private Date createdDate = Calendar.getInstance().getTime();
		private Date expiredDate = null;
		private int value;
		private TimeDependencyType type;

		@Override
		public Boolean isExpired() {
			if (expiredDate == null)
				return true;
			return (createdDate.compareTo(expiredDate) >= 0);
		}

		public void add(int value, TimeDependencyType type) {
			this.value = value;
			this.type = type;
			Calendar cal = Calendar.getInstance();
			cal.setTime(createdDate);
			switch (type) {
			case DAY: {
				cal.add(Calendar.DAY_OF_YEAR, value);
				break;
			}
			case HOUR: {
				cal.add(Calendar.HOUR, value);
				break;
			}
			case MINUTE: {
				cal.add(Calendar.MINUTE, value);
				break;
			}
			case MONDAY: {
				cal.add(Calendar.MONDAY, value);
				break;
			}
			case SECOND: {
				cal.add(Calendar.SECOND, value);
				break;
			}
			case WEEK: {
				cal.add(Calendar.WEEK_OF_YEAR, value);
				break;
			}
			}
			expiredDate = cal.getTime();
		}

		@Override
		public void restart() {
			createdDate = Calendar.getInstance().getTime();
			add(value, type);
		}
	}

	public static class NonExpiredDependency implements CacheDependecy {

		@Override
		public Boolean isExpired() {
			return false;
		}

		@Override
		public void restart() {
		}
	}

	private K key;
	private V data;
	private CacheDependecy dependency = new NonExpiredDependency();
	private CacheListener<V> listener;

	public CacheObject() {

	}

	public CacheObject(K key, V value) {
		this();
		if(key == null)
			throw new NullPointerException("key not null");
		this.key = key;
		this.data = value;
	}

	public CacheObject(K key, CacheListener<V> listener) {
		this();
		if(listener == null)
			throw new NullPointerException("listener not null");
		if(key == null)
			throw new NullPointerException("key not null");
		this.key = key;
		this.data = listener.getData();
		this.listener = listener;
	}

	public CacheObject(K key, V value, CacheDependecy dependency) {
		this();
		if(key == null)
			throw new NullPointerException("key not null");
		this.key = key;
		this.data = value;
		this.dependency = dependency;
	}

	public CacheObject(K key, CacheDependecy dependency, CacheListener<V> listener) {
		this();
		if(listener == null)
			throw new NullPointerException("listener not null");
		if(key == null)
			throw new NullPointerException("key not null");
		this.key = key;
		this.data = listener.getData();
		this.dependency = dependency;
		this.listener = listener;
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getData() {
		if (!dependency.isExpired())
			return data;
		else if (listener != null) {
			this.data = listener.getData();
			dependency.restart();
			return data;
		} else
			return null;
	}

	public void setData(V value) {
		if(listener!=null)
			throw new RuntimeException("listener available");
		this.data = value;
	}

	public CacheDependecy getDependency() {
		return dependency;
	}

	public void setDependency(CacheDependecy dependency) {
		this.dependency = dependency;
	}

	public CacheListener<V> getListener() {
		return listener;
	}

	public void setListener(CacheListener<V> listener) {
		this.listener = listener;
		if (listener != null) {
			this.data = listener.getData();
		}
	}

	public Boolean isExpired() {
		return dependency.isExpired();
	}
}

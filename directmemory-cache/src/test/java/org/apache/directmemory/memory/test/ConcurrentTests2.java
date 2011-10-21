package org.apache.directmemory.memory.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.directmemory.measures.Ram;
import org.apache.directmemory.memory.MemoryManager;
import org.apache.directmemory.memory.OffHeapMemoryBuffer;
import org.apache.directmemory.memory.Pointer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;
import com.google.common.collect.MapMaker;

@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart()
@BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY, maxRuns = 5)

public class ConcurrentTests2 {
	
	private final static int entries = 100000;
	public static AtomicInteger count = new AtomicInteger();
	private static AtomicInteger got = new AtomicInteger(); 
	private static AtomicInteger missed = new AtomicInteger(); 
	private static AtomicInteger good = new AtomicInteger(); 
	private static AtomicInteger bad = new AtomicInteger(); 
	private static AtomicInteger read = new AtomicInteger();

	public static ConcurrentMap<String, Pointer> map = new MapMaker()
		.concurrencyLevel(4)
		.initialCapacity(100000)
		.makeMap();

	
	@BenchmarkOptions(benchmarkRounds = 100000, warmupRounds=0, concurrency=100)
  	@Test
  	public void store() {
  		final String key = "test-" + count.incrementAndGet();
  		put(key);
  	}
	
	@BenchmarkOptions(benchmarkRounds = 1000000, warmupRounds=0, concurrency=100)
  	@Test
  	public void retrieveCatchThemAll() {
  		String key = "test-" + (rndGen.nextInt(entries)+1);
  		Pointer p = map.get(key);
		read.incrementAndGet();
  		if (p != null) {
  			got.incrementAndGet();
  			byte [] payload = MemoryManager.retrieve(p);
  	  		if (key.equals(new String(payload)))
  	  			good.incrementAndGet();
  	  		else
  	  			bad.incrementAndGet();
  		} else {
  			logger.info("did not find key " + key);
  			missed.incrementAndGet();
  		}
  	}
	
	@BenchmarkOptions(benchmarkRounds = 1000000, warmupRounds=0, concurrency=100)
  	@Test
  	public void retrieveCatchHalfOfThem() {
  		String key = "test-" + (rndGen.nextInt(entries*2)+1);
  		Pointer p = map.get(key);
		read.incrementAndGet();
  		if (p != null) {
  			got.incrementAndGet();
  			byte [] payload = MemoryManager.retrieve(p);
  	  		if (key.equals(new String(payload)))
  	  			good.incrementAndGet();
  	  		else
  	  			bad.incrementAndGet();
  		} else {
  			missed.incrementAndGet();
  		}
  	}
	
	private void put(String key) {
  		map.put(key, MemoryManager.store(key.getBytes()));
	}
  
	@BenchmarkOptions(benchmarkRounds = 1000000, warmupRounds=0, concurrency=10)
  	@Test
  	public void write3Read7() {
  		String key = "test-" + (rndGen.nextInt(entries*2)+1);
  		
  		int what = rndGen.nextInt(10);
  		
  		switch (what) {
			case 0: 
			case 1: 
			case 2: 
  				put(key);
  				break; 
  			default:
  				get(key);
  				break;
  				
  		}
  		
  	}
  
	@BenchmarkOptions(benchmarkRounds = 1000000, warmupRounds=0, concurrency=10)
  	@Test
  	public void write1Read9() {
  		String key = "test-" + (rndGen.nextInt(entries*2)+1);
  		
  		int what = rndGen.nextInt(10);
  		
  		switch (what) {
			case 0: 
  				put(key);
  				break; 
  			default:
  				get(key);
  				break;
  				
  		}
  		
  	}
	private void get(String key) {
  		Pointer p = map.get(key);
		read.incrementAndGet();
  		if (p != null) {
  			got.incrementAndGet();
  			byte [] payload = MemoryManager.retrieve(p);
  	  		if (key.equals(new String(payload)))
  	  			good.incrementAndGet();
  	  		else
  	  			bad.incrementAndGet();
  		} else {
  			missed.incrementAndGet();
  		}
	}

	Random rndGen = new Random();
	
	@Rule
	public MethodRule benchmarkRun = new BenchmarkRule();


	private static Logger logger = LoggerFactory.getLogger(ConcurrentTests2.class);

	private static void dump(OffHeapMemoryBuffer mem) {
		logger.info("off-heap - buffer: " + mem.bufferNumber);
		logger.info("off-heap - allocated: " + Ram.inMb(mem.capacity()));
		logger.info("off-heap - used:      " + Ram.inMb(mem.used()));
		logger.info("heap 	  - max: " + Ram.inMb(Runtime.getRuntime().maxMemory()));
		logger.info("heap     - allocated: " + Ram.inMb(Runtime.getRuntime().totalMemory()));
		logger.info("heap     - free : " + Ram.inMb(Runtime.getRuntime().freeMemory()));
		logger.info("************************************************");
	}
	
	@BeforeClass
	public static void init() {
		MemoryManager.init(1, Ram.Mb(512));
	}
	
	@AfterClass
	public static void dump() {
		
		for (OffHeapMemoryBuffer mem : MemoryManager.getBuffers()) {
			dump(mem);
		}
		
		logger.info("************************************************");
		logger.info("entries: " + entries);
		logger.info("inserted: " + map.size());
		logger.info("reads: " + read);
		logger.info("count: " + count);
		logger.info("got: " + got);
		logger.info("missed: " + missed);
		logger.info("good: " + good);
		logger.info("bad: " + bad);
		logger.info("************************************************");
	}

}
	
	


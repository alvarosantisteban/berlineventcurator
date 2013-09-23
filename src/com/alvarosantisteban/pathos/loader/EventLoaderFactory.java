package com.alvarosantisteban.pathos.loader;


public class EventLoaderFactory {
		

	public static EventLoader newArtParasitesEventLoader() {
		return new ArtParasitesEventLoader();
	}

	public static EventLoader newIHeartBerlinEventLoader() {
		return new IHeartBerlinEventLoader();
	}
	
	public static EventLoader newMetalConcertsEventLoader() {
		return new MetalConcertsEventLoader();
	}

	public static EventLoader newWhiteTrashEventLoader() {
		return new WhiteTrashEventLoader();
	}
	
	public static EventLoader newKoepiEventLoader() {
		return new KoepiEventLoader();
	}
	
	public static EventLoader newGothDatumEventLoader() {
		return new GothDatumEventLoader();
	}

	public static EventLoader newStressFaktorEventLoader() {
		return new StressFaktorEventLoader();
	}
	
	public static EventLoader newIndexEventLoader() {
		return new IndexEventLoader();
	}
}

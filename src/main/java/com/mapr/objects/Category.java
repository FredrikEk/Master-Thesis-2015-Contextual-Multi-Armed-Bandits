package com.mapr.objects;

import java.util.ArrayList;

public class Category {

	public final static int numberOfCategories = 33;
	
	public enum MainCategories {
		Accessories(1),
		SwimmersItch(2),
		BooksMovies_Music(4),
		Trousers(5),
		Shorts(29),
		Bicycles(6),
		Drinks(26),
		Helmets_Protection(7),
		Headphones(8),
		Jackets(9),
		Suits(10),
		Beanies_Scarves(11),
		Dresses_Skirts(12),
		Toys_Games(13),
		Watches(14),
		Skateboard(15),
		Shirts_Pique(16),
		Shoes(17),
		Snowboard(18),
		SnowSkate(19),
		Sunglasses_Goggle(20),
		Shirts(21),
		TShirts_Chemise(22),
		Lingerie(23),
		Bags(24),
		Wakeboard_WaterSport(25),
		ShoeAndClothingCare(27),
		Longboard(28),
		Photo_Film(30),
		Gloves_Mittens(31),
		Caps(32),
		Cruiser(33),
		Ski(3),
		None(255);
		int id;
		private MainCategories(int i){id = i;}
		public int GetID(){return id;}
		public boolean IsEmpty(){return this.equals(MainCategories.None);}
		public boolean Compare(int i){return id == i;}
		public static MainCategories GetValue(int _id)
		{
		    MainCategories[] As = MainCategories.values();
		    for(int i = 0; i < As.length; i++)
		    {
		        if(As[i].Compare(_id))
		            return As[i];
		    }
		    return MainCategories.None;
		}
	        // max: Ski - 34
	    }
	  
	public enum Categories {
		   
		BROKEN(0),
		//-Accessories-
		Belts(8),
		Wallets(7),
		BandAnas(2),
		Bracelets(1),
		Jewelry(10),
		Ties(9),
		Stickers(5),
		ClothMarks(11),
		Pins(6),
		AccessoriesMobilePhone(152),
		AccessoriesOther(13),
		//-SwimmersItch-
		Swimmingshorts(15),
		Bikinis(16),
		BathTowel(14),
		//-BooksMovies_Music-       
		CD(24),
		DVD(25),
		Books(23),
		Newspapers(26),
		//-Bicycles-
		Bicycles(27),
		//Motorcycles(28),                      
		BicyclesAccessories(29),
		EnergyDrinks(142),
		//-Helmets_Protection-
		Helmets(30),
		Protection(31),
		HelmetsAccessories(32),
		//-Toys_Games-
		FingerBoards(34),
		FingerBoardRamps(33),
		Collectables(35),
		Games(36),
		Toys_GamesOther(37),
		//-Watches-
		WristWatches(38),
		ClocksOther(139),
		//-Skateboard-
		Skateboards(45),
		CompleteSkateboards(42),
		CompleteLongboards(44),
		SlalomSkates(47),
		Skateboards_OldSchool(144),
		Cruiser_Skateboards(39),
		Skateboard_Wheels(41),
		Bearings(43),
		Trucks(48),
		SkateLongBoardTools(49),
		Screws_Nuts(46),
		GripTape(40),
		Skateboard_Other(50),
		Longboard_Wheels(151),
		Longboard_Trucks(150),
		Longboard_Other(175),
		//-Headphones-
		Headphones(51),
		Earphones(52),
		HeadphonesOther(53),
		//-Jackets-
		Jackets(54),
		SnowboardJackets(56),
		Coats(55),
		ManCoats(141),
		Vests(148),
		Winter_jackets(149),
		//-Trousers-
		Jeans(59),
		Chino(58),
		Cargo(57),
		ManchesterTrousers(60),
		SnowboardPants(62),
		OtherPants(63),
		//-Dresses_Skirts-       
		Dresses(65),
		Skirts(64),
		Tunikor(138),
		//-Suits-
		SuitJackets(66),
		Suits(67),
		Waistcoats(68),
		CostumeOther(69),
		Beanies(72),
		HeadwearOther(137),
		//-Shoes-
		Highshoes(74),
		Lowshoes(77),
		Slipons(80),
		Ballerina(73),
		Boots(76),
		Heels(75),
		Sandals_FlipFlop(78),
		Slippers(82),
		ShoeLaces_Other(140),
		//-Shirts_Pique-
		Shortshirts(83),
		Shirts(84),
		Pike(85),
		//-Shirts-
		Ziphoods(91),
		Zipcrews_TrackTops(90),
		Hoodies(88),
		Crews_Sweatshirts(87),
		Cardigans(86),
		KnittedSweaters(89),
		//-TShirts_Chemise-
		ShortTshirts(92),
		LongTshirts(94),
		Linens(93),
		Tops(95),
		TallTees(143),
		//-Sunglasses_Goggle-
		Sunglasses(99),
		Goggles(97),
		MXGoggle(98),
		ReplacementLenses(96),
		SunglassesAccessories(100),
		//-SnowSkate-
		SnowKates(101),
		KateSnowShoes(102),
		SnowSkateOther(103),
		//-Snowboard-
		SnowBoards(108),
		SnowboardPackages(107),
		SnowboardBindings(104),
		SnowboardBoots(106),
		Boardbags(105),
		SnowboardTools(110),
		SnowboardAccessories_Other(109),
		//-Lingerie-
		Underpants(112),
		Panties(117),
		Socks(115),
		LingerieSet(114),
		Underwear(118),
		Pajamas(113),
		Bh(111),
		//-Bags-
		Backpacks(124),
		ShoulderBags(119),
		Handbags(121),
		TravellBags(123),
		Tygkassar(125),
		LaptopCases(122),
		Bags(120),
		BagsOther(126),
		//-Wakeboard_WaterSport-
		WakeBoards(133),
		WakeboardPackage(132),
		WakeKates(134),
		WakeboardBindings(127),
		LifeJacket_Wetsuits(135),
		Handles_Ropes(128),
		Surfboards(129),
		Tubes(131),
		WakeboardAccessories(130),
		//-OtherProducts-special-
		OtherProducts(136),
		ShoeCare(146),
		ClothingCare(147),
		Leggings(153),
		Sweatpants(154),
		TightTrousers(155),
		//-Shorts-
		ShortsDenim(156),
		ShortsChino(157),
		ShortsCargo(158),
		ShortsOther(159),
		//-Photo-
		Cameras(160),
		Stands_Mounts(161),
		Lenses_Accessories(162),
		PhotoBags(163),
		//-Gloves and Mittens
		Mittens(164),
		Gloves(176),
		//-Beanies and Scarves
		FaceMasks(165),
		Scarves(166),
		Headbands(167),
		//-Caps-
        Flexfit(168),
        Fitted(169),
        Snapback(170),
        Trucker(171),
        FivePanel(172),
        Hats(173),
        Visors(174),
 
        Longboards(177),
        CompleteCruisers(178),
        Cruiser_wheels(179),
        Cruiser_other(180),
        Caps_other(181),
		 
		        //-Ski-
		Skis(182),
		SkiPackages(183),
		SkiBindings(184),
		SkiBoots(185),
		SkiPoles(186),
		SkiOther(187),
		None(255);
		int id;
		private Categories(int i){id = i;}
		public int GetID(){return id;}
		public boolean IsEmpty(){return this.equals(Categories.None);}
		public boolean Compare(int i){return id == i;}
		public static Categories GetValue(int _id)
		{
		    Categories[] As = Categories.values();
		    for(int i = 0; i < As.length; i++)
		    {
		        if(As[i].Compare(_id))
		            return As[i];
		    }
		    return Categories.None;
		}
	    
	}
	
	public static ArrayList<Categories> GetByByteArray(String byteArray) {

		ArrayList<Categories> subCategories = new ArrayList<Categories>();

		int index = byteArray.indexOf("1");
		while (index >= 0) {
			index = byteArray.indexOf("1", index + 1);
			subCategories.add(Categories.GetValue(index));
		}
   
	   return subCategories;
   }
	   
	public static MainCategories GetMainCategoryByCategory(Categories c) {
		// ShoeCare belongs to 2 Main Categories! Let's pick the primary main
		// category (according to Junkyard):
		if (c == Categories.ShoeCare) {
			return MainCategories.ShoeAndClothingCare;
		}

		for (MainCategories m : MainCategories.class.getEnumConstants()) {
			Categories[] cs = GetByMainCategory(m);

			for (Categories temp : cs) {
				if (c == temp) {
					return m;
				}
			}
		}
		return MainCategories.GetValue(0);
	}
	   
	public static Categories[] GetByMainCategory(MainCategories m) {
		switch (m) {
		case Accessories:
			return new Categories[] { Categories.Belts, Categories.Wallets,
					Categories.BandAnas, Categories.Bracelets,
					Categories.Jewelry, Categories.Ties, Categories.Stickers,
					Categories.ClothMarks, Categories.Pins,
					Categories.AccessoriesMobilePhone,
					Categories.AccessoriesOther };
		case SwimmersItch:
			return new Categories[] { Categories.Swimmingshorts,
					Categories.Bikinis, Categories.BathTowel };
		case BooksMovies_Music:
			return new Categories[] { Categories.CD, Categories.DVD,
					Categories.Books, Categories.Newspapers };
		case Trousers:
			return new Categories[] { Categories.Jeans, Categories.Chino,
					Categories.Cargo, Categories.ManchesterTrousers,
					Categories.SnowboardPants, Categories.Leggings,
					Categories.Sweatpants, Categories.TightTrousers,
					Categories.OtherPants };
		case Shorts:
			return new Categories[] { Categories.ShortsDenim,
					Categories.ShortsChino, Categories.ShortsCargo,
					Categories.Swimmingshorts, Categories.ShortsOther };
		case Bicycles:
			return new Categories[] { Categories.Bicycles,
					Categories.BicyclesAccessories };
		case Drinks:
			return new Categories[] { Categories.EnergyDrinks };
		case Helmets_Protection:
			return new Categories[] { Categories.Helmets,
					Categories.Protection, Categories.HelmetsAccessories };
		case Photo_Film:
			return new Categories[] { Categories.Cameras,
					Categories.Stands_Mounts, Categories.Lenses_Accessories,
					Categories.PhotoBags };
		case Headphones:
			return new Categories[] { Categories.Headphones,
					Categories.Earphones, Categories.HeadphonesOther };
		case Jackets:
			return new Categories[] { Categories.Jackets,
					Categories.SnowboardJackets, Categories.Coats,
					Categories.ManCoats, Categories.Vests,
					Categories.Winter_jackets };
		case Suits:
			return new Categories[] { Categories.SuitJackets, Categories.Suits,
					Categories.Waistcoats };
		case Gloves_Mittens:
			return new Categories[] { Categories.Gloves, Categories.Mittens };
		case Beanies_Scarves:
			return new Categories[] { Categories.Beanies, Categories.FaceMasks,
					Categories.Scarves, Categories.Headbands,
					Categories.HeadwearOther, };
		case Caps:
			return new Categories[] { Categories.Flexfit, Categories.Fitted,
					Categories.Snapback, Categories.Trucker,
					Categories.FivePanel, Categories.Hats, Categories.Visors,
					Categories.Caps_other };
		case Dresses_Skirts:
			return new Categories[] { Categories.Dresses, Categories.Skirts,
					Categories.Tunikor };
		case Toys_Games:
			return new Categories[] { Categories.FingerBoards,
					Categories.FingerBoardRamps, Categories.Collectables,
					Categories.Games, Categories.Toys_GamesOther };
		case Watches:
			return new Categories[] { Categories.WristWatches,
					Categories.ClocksOther };
		case Skateboard:
			return new Categories[] { Categories.Skateboards,
					Categories.CompleteSkateboards,
					Categories.Skateboards_OldSchool, Categories.SlalomSkates,
					Categories.Trucks, Categories.Skateboard_Wheels,
					Categories.Bearings, Categories.Screws_Nuts,
					Categories.GripTape, Categories.SkateLongBoardTools,
					Categories.Skateboard_Other };
		case Longboard:
			return new Categories[] { Categories.Longboards,
					Categories.CompleteLongboards, Categories.Longboard_Trucks,
					Categories.Longboard_Wheels, Categories.Bearings,
					Categories.Screws_Nuts, Categories.GripTape,
					Categories.SkateLongBoardTools, Categories.Longboard_Other };
		case Shirts_Pique:
			return new Categories[] { Categories.Shortshirts,
					Categories.Shirts, Categories.Pike };
		case Shoes:
			return new Categories[] { Categories.Highshoes,
					Categories.Lowshoes, Categories.Slipons,
					Categories.Ballerina, Categories.Boots, Categories.Heels,
					Categories.Sandals_FlipFlop, Categories.Slippers,
					Categories.ShoeLaces_Other, Categories.ShoeCare };
		case Snowboard:
			return new Categories[] { Categories.SnowBoards,
					Categories.SnowboardPackages, Categories.SnowboardBindings,
					Categories.SnowboardBoots, Categories.Boardbags,
					Categories.SnowboardTools,
					Categories.SnowboardAccessories_Other };
		case SnowSkate:
			return new Categories[] { Categories.SnowKates,
					Categories.KateSnowShoes, Categories.SnowSkateOther };
		case Sunglasses_Goggle:
			return new Categories[] { Categories.Sunglasses,
					Categories.Goggles, Categories.MXGoggle,
					Categories.ReplacementLenses,
					Categories.SunglassesAccessories };
		case Shirts:
			return new Categories[] { Categories.Ziphoods,
					Categories.Zipcrews_TrackTops, Categories.Hoodies,
					Categories.Crews_Sweatshirts, Categories.Cardigans,
					Categories.KnittedSweaters, };
		case TShirts_Chemise:
			return new Categories[] { Categories.ShortTshirts,
					Categories.LongTshirts, Categories.Linens, Categories.Tops,
					Categories.TallTees };
		case Lingerie:
			return new Categories[] { Categories.Underpants,
					Categories.Panties, Categories.Socks,
					Categories.LingerieSet, Categories.Underwear,
					Categories.Pajamas, Categories.Bh };
		case Bags:
			return new Categories[] { Categories.Backpacks,
					Categories.ShoulderBags, Categories.Handbags,
					Categories.TravellBags, Categories.Tygkassar,
					Categories.LaptopCases, Categories.Bags,
					Categories.BagsOther };
		case Wakeboard_WaterSport:
			return new Categories[] { Categories.WakeBoards,
					Categories.WakeboardPackage, Categories.WakeKates,
					Categories.WakeboardBindings,
					Categories.LifeJacket_Wetsuits, Categories.Handles_Ropes,
					Categories.Surfboards, Categories.Tubes,
					Categories.WakeboardAccessories };
		case ShoeAndClothingCare:
			return new Categories[] { Categories.ShoeCare,
					Categories.ClothingCare };
		case Cruiser:
			return new Categories[] { Categories.Cruiser_Skateboards,
					Categories.CompleteCruisers, Categories.Trucks,
					Categories.Cruiser_wheels, Categories.Bearings,
					Categories.Screws_Nuts, Categories.GripTape,
					Categories.SkateLongBoardTools, Categories.Cruiser_other };
		case Ski:
			return new Categories[] { Categories.Skis, Categories.SkiPackages,
					Categories.SkiBindings, Categories.SkiBoots,
					Categories.SkiPoles, Categories.SkiOther };

		default:
			return new Categories[] {};
		}

	}
}

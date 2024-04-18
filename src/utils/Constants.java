package utils;

public class Constants {

	public static class Resources {

		public static final int GOLD_MINE = 0;

		public static int getHitboxHeight(int resourceType) {
			switch (resourceType) {
			case GOLD_MINE:
				return 32;
			}
			return 0;

		}

		public static int getHitboxWidth(int resourceType) {
			switch (resourceType) {
			case GOLD_MINE:
				return 32;
			}
			return 0;

		}

		public static int getStartingTotal(int resourceType) {
			switch (resourceType) {
			case GOLD_MINE:
				return 1000;
			}
			return 0;
		}

		public static int getAmountPerAction(int resourceType) {
			switch (resourceType) {
			case GOLD_MINE:
				return 1;
			}
			return 0;
		}

	}

}

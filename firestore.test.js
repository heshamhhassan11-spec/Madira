const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read users profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice Star",
      email: "alice@test.com",
      avatarEmoji: "⚽",
      favoriteClub: "الأهلي المصري",
      points: 100,
      winStreak: 2,
      virtualCoins: 200.0,
      favoriteChallenges: ["guess_player"],
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated user: cannot create profile for another user ID", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob Fake",
      email: "bob@test.com",
      avatarEmoji: "⚽",
      favoriteClub: "الزمالك",
      points: 50,
      winStreak: 1,
      virtualCoins: 200.0,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated user: cannot read another user's private profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob Player",
      email: "bob@test.com",
      avatarEmoji: "⚽",
      favoriteClub: "الزمالك",
      points: 200,
      winStreak: 3,
      virtualCoins: 200.0,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can read public leaderboard", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("leaderboard").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob Player",
      avatarEmoji: "⚽",
      favoriteClub: "الزمالك",
      points: 200,
      winStreak: 3,
      weeklyWinRate: "80%",
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("leaderboard").doc(BOB_UID).get());
  await assertSucceeds(aliceDb.collection("leaderboard").get());
});

test("Unauthenticated user: cannot read leaderboard", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("leaderboard").doc(BOB_UID).get());
});

test("Authenticated user: can update own leaderboard entry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice Star",
      avatarEmoji: "⚽",
      favoriteClub: "الأهلي المصري",
      points: 150,
      winStreak: 2,
      weeklyWinRate: "85%",
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      points: 250,
      winStreak: 3,
      updatedAt: new Date(),
    })
  );
});

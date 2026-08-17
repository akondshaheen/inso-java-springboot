# INSO — Software Engineer Interview Assessment & Preparation Guide

**Company:** INSO — International NGO Safety Organization (https://ngosafety.org/)
**Role:** Software Engineer — The Hague, Netherlands (on site, fixed term, full time)
**Product:** The INSO Portal — a long-lived platform used by NGO partners, INSO field
teams, and HQ staff. Real-time incident tracking, analytical reports, safety data and
mapping, crisis management support.

---

## 0. What we actually know about the exercise (and what it means)

An INSO software engineer (one of three on the team) told you directly:

> "The exercise is mostly **Java-language and logic focused** — **collections, filtering
> and aggregating data, and some light algorithmic thinking**. It's set up as a **Spring
> Boot project**, so a basic familiarity with **how services and repositories fit
> together** helps, but **you won't be building APIs or endpoints** — the focus is on
> **implementing the business logic inside the service**. Just have **Java 17, Maven
> 3.x, and your IDE ready**. It's structured as **incremental steps**, and **your
> thought process matters as much as a finished solution** — feel free to **think out
> loud and ask questions**."

### Decoding this, line by line

| What he said | What it really means for your prep |
|---|---|
| "mostly Java-language and logic focused" | Not a framework quiz. No Spring internals trivia. Core Java: collections, streams, control flow, null handling, `Optional`, records, generics. |
| "collections, filtering and aggregating data" | **This is the heart of it.** `List`/`Set`/`Map`, `Stream.filter`, `Collectors.groupingBy`, `counting`, `summingInt`, `averagingDouble`, `toMap`, sorting with `Comparator`. Expect "group incidents by country and count them", "find the top 3 X", "average per month". |
| "some light algorithmic thinking" | Not LeetCode Hard. Think: frequency maps, sorting + picking top-N, merging/overlapping intervals, deduplicating, a simple sliding window or two-pointer, basic Big-O reasoning. |
| "set up as a Spring Boot project" | You will open a Maven project, it will boot, there will be `@Service` and repository classes. You must not be confused by the wiring. |
| "you won't be building APIs or endpoints" | **Do not waste time** on `@RestController`, `ResponseEntity`, validation annotations, JSON. Zero controllers. |
| "focus is on implementing the business logic inside the service" | You will be given method stubs in a `...Service` class (probably with failing tests or a `main`) and asked to fill them in. |
| "Java 17, Maven 3.x, IDE ready" | Java 17 features are fair game and *welcome*: records, enhanced `switch`, `instanceof` pattern matching, text blocks, `var`, sealed types, `Stream.toList()`. |
| "structured as incremental steps" | Step 1 easy, step 2 builds on it, step 3 adds a twist (grouping, sorting, edge case), step 4 may add performance or a tricky requirement. **Do not over-engineer step 1** — you'll need the time later. |
| "your thought process matters as much as a finished solution" | **Talk constantly.** State assumptions. Name edge cases out loud even if you don't handle them. Say "I'd normally add X, is that in scope?" |
| "feel free to think out loud and ask questions" | Asking clarifying questions is *scored positively*. Silence is the biggest failure mode. |

### The single most likely shape of the exercise

A `Repository` returns a `List<SomeRecord>` of in-memory data (incidents, reports,
organisations, staff, countries). A `Service` has 4–6 stub methods. You implement them
incrementally:

1. **Filter** — "return all incidents in a given country"
2. **Aggregate** — "count incidents per country" → `Map<String, Long>`
3. **Sort / Top-N** — "return the 3 countries with the most incidents, ties broken alphabetically"
4. **Multi-level / derived** — "average severity per country per month", or "percentage change month over month"
5. **Algorithmic twist** — "find the longest run of consecutive days with no incident", or "merge overlapping travel-restriction date ranges"

Everything in this document is built around being able to do all five, cold, while talking.

---

## 1. Environment readiness checklist (do this BEFORE the interview)

Run these and make sure they work. Do it the day before, not 10 minutes before.

```bash
java -version        # must show 17.x  (or 21 with --release 17 — but aim for exactly 17)
javac -version
mvn -version         # must show Maven 3.x and point at the Java 17 JDK
echo $JAVA_HOME      # Windows: echo %JAVA_HOME%
```

**Checklist:**

- [ ] JDK **17** installed and `JAVA_HOME` points at it (not a JRE, not Java 8, not 11).
- [ ] Maven **3.x** on the `PATH`. `mvn -version` shows the Java 17 JDK.
- [ ] IDE (IntelliJ IDEA recommended) opens a Maven project and auto-imports.
- [ ] IDE **Project SDK = 17** and **Language level = 17** (IntelliJ: File → Project Structure).
- [ ] `mvn clean test` works on *some* project offline — **pre-warm your `~/.m2` cache**.
      If the exercise repo needs to download Spring Boot dependencies on a slow
      conference Wi-Fi, you lose 5 minutes. Build your `Backend/core` project once the
      morning of the interview so Spring Boot + JUnit + Mockito jars are already local.
- [ ] Know your IDE shortcuts cold: run a single test, run all tests, rename symbol,
      generate constructor, extract method, reformat, "show me the errors".
- [ ] Screen-share tested. Font size increased so the interviewer can read it.
- [ ] A scratch file / second window where you can jot notes without polluting the code.

**Pre-warm command (run in `Backend/core` the morning of):**

```bash
mvn -q clean test
```

---

## 2. How to run the interview itself (the meta-skill they are actually scoring)

He told you the thought process matters *as much as* the solution. Treat that literally.

### 2.1 The opening 60 seconds of each step

Before writing a single character:

1. **Read the whole stub + its Javadoc + its test out loud.** Slowly.
2. **Restate the requirement in your own words:** *"So I need to return, for each
   country, how many incidents happened there — as a map from country name to count.
   Is that right?"*
3. **Ask the clarifying questions** (see the list in §2.2).
4. **State your approach before coding:** *"I'll stream the list, group by country with
   `Collectors.groupingBy`, and use `Collectors.counting()` as the downstream collector.
   That's O(n) with one pass."*
5. **Then** code.

### 2.2 The clarifying-question bank (memorise 6–8 of these)

Ask the ones that are relevant. Asking 2–3 good questions per step is ideal; asking 10
is stalling.

**About the data:**
- "Can the input list be `null`, or is it guaranteed non-null but possibly empty?"
- "Can individual fields be `null` — e.g. can an incident have no country, or no date?"
- "Are duplicates possible? Is there a unique ID I should dedupe on?"
- "Is the input already sorted in any way, or should I assume arbitrary order?"
- "Roughly how large is this data — hundreds, or millions? That changes whether I care
  about a second pass."

**About the output:**
- "Should I return an empty collection or `null` when there are no matches?"
  *(Always argue for empty — never return `null` collections.)*
- "Does the ordering of the result matter? Should it be insertion order, sorted, or is
  it unspecified?"
- "Should the returned collection be mutable or should I return an unmodifiable view?"
- "If fewer than N results exist, should I return what I have, or is that an error?"

**About the rules:**
- "How should ties be broken?" — *(This is the #1 hidden requirement in top-N questions.)*
- "Is the comparison case-sensitive? Should 'Mali' and 'mali' be the same country?"
- "Are date ranges inclusive or exclusive at the end?"
- "What's the expected behaviour for the average of an empty group — 0, `NaN`, or
  `Optional.empty()`?"
- "Should invalid records be skipped silently, or should the method throw?"

**About scope:**
- "Do you want me to optimise this now, or get it correct first and revisit?"
- "Would you like me to write a quick test for this, or is the provided test enough?"

### 2.3 Phrases that make you sound senior

- *"Let me state my assumptions before I code…"*
- *"The naive approach is X, which is O(n²). A `Map`-based approach makes it O(n). Given
  the data size, I'll start with the simple one and we can optimise if you want."*
- *"I'm going to handle the happy path first, then come back to null/empty handling —
  just so you know I haven't forgotten it."*
- *"Edge cases I can see here are: empty list, ties, and null country. Which of those
  matter for this exercise?"*
- *"I'd normally push this validation into the service boundary rather than repeating it
  in every method."*
- *"That's a design decision — I'll go with X because Y, but Z would also be defensible."*

### 2.4 If you get stuck

Do **not** go silent. Say:

- *"Let me talk through this out loud — I'm weighing two approaches…"*
- *"I know `groupingBy` takes a downstream collector; I want the one that maps values
  after grouping. Let me check the `Collectors` class in the IDE."*
  **Using autocomplete / Javadoc in the IDE is completely fine and looks professional.**
  Pretending to remember every `Collectors` signature is not the test.
- *"Can I write it the long way with a for-loop first, and refactor to streams after?"*
  **A correct loop beats a broken stream every single time.** Say this explicitly.

### 2.5 Time management across incremental steps

- Step 1 is a warm-up. **Do not gold-plate it.** No custom exceptions, no logging
  framework, no builder patterns. Simple, correct, readable.
- Mention what you *would* add ("in production I'd add input validation here") instead
  of building it. That earns the same credit for a fraction of the time.
- If you finish a step early, say: *"I'm happy with this — want me to add a test, or
  shall we move to the next step?"* Let them steer.

---

## 3. Java 17 language features you should actively use (and be able to explain)

Using these correctly signals "this person writes modern Java". Using them *wrongly*
signals the opposite, so know the limits of each.

### 3.1 `record` — the single most useful feature for this exercise

```java
public record Incident(
        Long id,
        String country,
        String type,
        Severity severity,
        LocalDate date,
        int casualties
) {}
```

**What the compiler generates for you:** a canonical constructor, one accessor per
component (`incident.country()` — **no `get` prefix**), `equals()`, `hashCode()`, and
`toString()`.

**Key facts to be able to say out loud:**
- Records are **implicitly `final`** — they cannot be extended, and they cannot extend
  another class (they already extend `java.lang.Record`). They *can* implement interfaces.
- All fields are **`private final`**. Records are **shallowly immutable**: if a component
  is a `List`, the reference can't change but the list contents can, unless you defend
  against it.
- `equals()` compares **all components**, so records work perfectly as `HashMap` keys and
  in `HashSet` — this matters enormously for grouping and deduplication.
- You can add a **compact constructor** for validation/normalisation:

```java
public record Incident(Long id, String country, Severity severity, LocalDate date) {
    public Incident {                                  // compact constructor — no params, no assignment
        Objects.requireNonNull(country, "country must not be null");
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Incident date cannot be in the future");
        }
        country = country.trim();                      // normalising a component is allowed here
    }
}
```

- You can add **static factories, extra methods, and even override accessors**:

```java
public record DateRange(LocalDate start, LocalDate end) {
    public boolean overlaps(DateRange other) {
        return !end.isBefore(other.start) && !other.end.isBefore(start);
    }
    public long days() { return ChronoUnit.DAYS.between(start, end) + 1; }
}
```

- **Defensive copy for collection components:**

```java
public record Report(String title, List<String> tags) {
    public Report {
        tags = tags == null ? List.of() : List.copyOf(tags);   // now truly immutable
    }
}
```

- **Local records** are legal — declare a record *inside a method* as a throwaway tuple.
  This is a fantastic trick for multi-key grouping (see §5.7):

```java
public Map<String, Long> countByCountryAndMonth(List<Incident> incidents) {
    record Key(String country, YearMonth month) {}   // local record — scoped to this method
    return incidents.stream()
            .collect(Collectors.groupingBy(
                    i -> new Key(i.country(), YearMonth.from(i.date())),
                    Collectors.counting()))
            .entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().country() + "/" + e.getKey().month(),
                                      Map.Entry::getValue));
}
```

**Likely interview question:** *"When would you use a record and when a class?"*
> A record when the type's identity **is** its data — DTOs, value objects, keys,
> read-only query results. A regular class when you need mutability, inheritance,
> lazily-computed or hidden internal state, or when `equals` should be based on an ID
> rather than every field (JPA entities are the classic example — I'd keep those as
> classes).

### 3.2 `enum` — better than `String` constants for severity/status/type

```java
public enum Severity {
    LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);

    private final int weight;
    Severity(int weight) { this.weight = weight; }     // enum constructors are implicitly private
    public int weight() { return weight; }

    public boolean isAtLeast(Severity other) { return this.weight >= other.weight; }
}
```

- Enums are **singletons per constant**, so `==` comparison is safe and preferred over
  `.equals()`.
- `ordinal()` exists but **don't rely on it for business logic** — reordering constants
  silently changes behaviour. Give an explicit field like `weight` instead. Say this out
  loud if you use an enum; it's a strong signal.
- `values()` returns a **new array each call** (it's a defensive copy) — don't call it in
  a tight loop.
- `EnumMap` / `EnumSet` are dramatically faster than `HashMap`/`HashSet` for enum keys
  (array-backed, no hashing). Mentioning `EnumMap` when grouping by an enum is an easy
  senior-sounding point.

```java
Map<Severity, Long> bySeverity = incidents.stream()
        .collect(Collectors.groupingBy(Incident::severity,
                 () -> new EnumMap<>(Severity.class),        // ordered by enum declaration, no hashing
                 Collectors.counting()));
```

- Safe parsing from a string (a very common exercise sub-task):

```java
public static Optional<Severity> parse(String raw) {
    if (raw == null) return Optional.empty();
    return Arrays.stream(values())
            .filter(s -> s.name().equalsIgnoreCase(raw.trim()))
            .findFirst();
}
// NOT: Severity.valueOf(raw) — that throws IllegalArgumentException on bad input, and NPE on null.
```

### 3.3 Enhanced `switch` (Java 14+, standard in 17)

```java
// switch EXPRESSION — returns a value, arrow labels, no fall-through, no break needed
int priority = switch (severity) {
    case CRITICAL, HIGH -> 1;
    case MEDIUM         -> 2;
    case LOW            -> 3;
};
```

- Over an enum, a switch **expression** must be **exhaustive**; if you cover every
  constant you don't need a `default`, and the compiler will then *error* if someone adds
  a new constant later. That's a genuine safety benefit — say it.
- Use `yield` when a branch needs a block:

```java
String label = switch (status) {
    case OPEN -> "Open";
    case CLOSED -> {
        var suffix = closedByUser == null ? "" : " by " + closedByUser;
        yield "Closed" + suffix;
    }
};
```

- Arrow form has **no fall-through**, which removes the classic missing-`break` bug.

### 3.4 Pattern matching for `instanceof`

```java
// Old
if (o instanceof Incident) {
    Incident i = (Incident) o;
    return i.country();
}
// Java 17
if (o instanceof Incident i && i.severity() == Severity.CRITICAL) {
    return i.country();      // 'i' is in scope and already cast
}
```

The binding variable's scope is *flow-sensitive* — it's available wherever the compiler
can prove the check passed, including after an early return:

```java
if (!(o instanceof Incident i)) return List.of();
return List.of(i.country());   // 'i' is legal here
```

### 3.5 `var` — use it with judgement

```java
var byCountry = new HashMap<String, List<Incident>>();   // good: removes noisy duplication
var x = repo.find();                                     // bad: reader can't tell the type
```
Rule of thumb: use `var` when the right-hand side makes the type obvious. Say *"I use
`var` where it reduces noise without hiding the type"* if asked.

### 3.6 Text blocks

```java
String jpql = """
        SELECT i FROM Incident i
        WHERE i.country = :country
          AND i.date BETWEEN :from AND :to
        """;
```
Rarely needed here (no endpoints), but nice for a multi-line JPQL query or test fixture.

### 3.7 Sealed types (know the concept, you probably won't need it)

```java
public sealed interface Event permits IncidentEvent, TrainingEvent, AlertEvent {}
```
Restricts which types may implement an interface, letting `switch` be exhaustive over a
closed hierarchy. One sentence is enough: *"Sealed types let me model a closed set of
subtypes so the compiler can check I've handled all of them."*

### 3.8 Small Java 17 API wins

| API | Use |
|---|---|
| `Stream.toList()` | Java 16+. Shorter than `collect(Collectors.toList())`. **Returns an unmodifiable list** — that's the difference, and it's a great thing to point out. |
| `List.of()`, `Map.of()`, `Set.of()` | Java 9+. Immutable, **reject `null` elements**, `Map.of` rejects duplicate keys. Great for test fixtures and empty returns. |
| `List.copyOf(x)` | Immutable defensive copy. |
| `Collectors.toUnmodifiableList/Set/Map` | Explicit immutable results. |
| `Objects.requireNonNull(x, "msg")` | Fail fast with a clear message. |
| `Objects.requireNonNullElse(x, fallback)` | Null-coalescing without an `if`. |
| `Objects.equals(a, b)` / `Objects.hash(...)` | Null-safe equality/hashing. |
| `String.isBlank()`, `strip()`, `repeat()`, `lines()` | Java 11 string helpers. `isBlank()` beats `trim().isEmpty()`. |
| `Optional.or()`, `ifPresentOrElse()`, `stream()` | Java 9+ `Optional` upgrades. |
| `Map.getOrDefault / computeIfAbsent / merge / putIfAbsent` | Your best friends for manual aggregation (§4.6). |

---

## 4. Collections — deep dive (the core of the exercise)

### 4.1 The map of the Collections Framework

```
Iterable
  └── Collection
        ├── List   — ordered, index-based, duplicates allowed
        │     ├── ArrayList     — resizable array. O(1) get, O(1) amortised add-at-end
        │     ├── LinkedList    — doubly linked list. O(1) add/remove at ends, O(n) get
        │     └── (List.of(...)) — immutable
        ├── Set    — no duplicates, at most one null (depending on impl)
        │     ├── HashSet       — backed by HashMap. O(1) ops, NO order guarantee
        │     ├── LinkedHashSet — HashSet + insertion order
        │     └── TreeSet       — red-black tree, SORTED, O(log n), NavigableSet
        └── Queue / Deque
              ├── ArrayDeque      — best general-purpose stack/queue
              └── PriorityQueue   — heap; O(log n) offer/poll, peek smallest. TOP-K TOOL.

Map (NOT a Collection)
  ├── HashMap        — O(1) average. One null key, many null values. No order.
  ├── LinkedHashMap  — insertion order (or access order for LRU caches)
  ├── TreeMap        — sorted by key, O(log n), NavigableMap (floorKey, headMap, ...)
  ├── EnumMap        — array-backed, enum keys only, very fast, enum-declaration order
  └── ConcurrentHashMap — thread-safe, no null keys or values
```

### 4.2 Complexity table (be ready to recite the important rows)

| Operation | ArrayList | LinkedList | HashSet/HashMap | TreeSet/TreeMap |
|---|---|---|---|---|
| get by index | **O(1)** | O(n) | n/a | n/a |
| add at end | O(1)* | O(1) | O(1)* | O(log n) |
| add at index / front | O(n) | O(1) at ends | n/a | n/a |
| `contains` / `containsKey` | **O(n)** | O(n) | **O(1)*** | O(log n) |
| remove by value | O(n) | O(n) | O(1)* | O(log n) |
| iterate all | O(n) | O(n) | O(n) | O(n), sorted |

`*` = amortised / average. `HashMap` degrades to O(log n) per bucket in Java 8+ once a
bucket exceeds 8 entries (it converts to a balanced tree) — used to be O(n).

**The single most valuable practical takeaway:** if you find yourself calling
`list.contains(x)` inside a loop over another list, that's O(n·m). Build a `HashSet`
first and it becomes O(n+m). **Say this out loud if you spot it** — it is exactly the
"light algorithmic thinking" they mentioned.

### 4.3 Choosing the right collection — the decision script

| Requirement | Choice | Why |
|---|---|---|
| Ordered list, mostly reads/appends | `ArrayList` | Cache-friendly, O(1) access |
| Heavy insert/remove at both ends | `ArrayDeque` (not `LinkedList`) | Better constants, less memory |
| Uniqueness, order irrelevant | `HashSet` | O(1) |
| Uniqueness + keep insertion order | `LinkedHashSet` | Deterministic output = testable |
| Uniqueness + sorted output | `TreeSet` | Sorted iteration for free |
| Key → value lookup | `HashMap` | O(1) |
| Deterministic, insertion-ordered map output | `LinkedHashMap` | **Great for interview output stability** |
| Sorted-by-key map, or range queries | `TreeMap` | `firstKey`, `headMap`, `floorEntry` |
| Enum keys | `EnumMap` | Fastest, ordered by declaration |
| Fixed constants / empty return | `List.of()`, `Map.of()` | Immutable, expresses intent |
| Top-K from a huge stream | `PriorityQueue` size K | O(n log k) instead of O(n log n) |

**Interview soundbite:** *"I default to `ArrayList` and `HashMap`. I reach for
`LinkedHashMap` when the output order needs to be deterministic — which usually matters
for anything a user or a test will look at — and `TreeMap` when I need the keys sorted or
need range lookups."*

### 4.4 `equals()` / `hashCode()` — the contract (near-guaranteed question)

**The contract:**
1. If `a.equals(b)` is true, then `a.hashCode() == b.hashCode()` **must** be true.
2. The reverse is **not** required — unequal objects may share a hash code (a collision).
3. `equals` must be reflexive, symmetric, transitive, consistent, and `x.equals(null)`
   must be `false`.
4. `hashCode` must be consistent for the object's lifetime **as long as the fields used
   in `equals` don't change**.

**Why it matters here:** `HashMap`, `HashSet`, `distinct()`, `groupingBy` keys and
`Collectors.toMap` keys **all** depend on it.

**How `HashMap.get` actually works** (say this if asked "how does HashMap work?"):
> It calls `hashCode()` on the key, spreads the bits to reduce collisions, and uses the
> result modulo the table size to pick a bucket. Inside the bucket it walks the entries
> comparing first by hash then with `equals()`. So a broken `hashCode` means the lookup
> goes to the wrong bucket and the entry is never found, even though `equals` would have
> said yes.

**The classic bug to name out loud:**
```java
Set<Incident> set = new HashSet<>();
var i = new MutableIncident("Mali");
set.add(i);
i.setCountry("Chad");     // hashCode changed AFTER insertion
set.contains(i);          // false! The object is lost in the wrong bucket.
```
> "This is why keys should be immutable — and one more reason I like records for keys."

**Correct implementation for a normal class:**
```java
@Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Incident other)) return false;   // Java 17 pattern matching
    return Objects.equals(id, other.id);
}
@Override public int hashCode() { return Objects.hash(id); }
```
*(For records you get all of this free — that's the point.)*

**JPA nuance worth one sentence:** for entities, base `equals`/`hashCode` on a business
key or return a constant `hashCode`, because the generated `id` is `null` before the
entity is persisted, which breaks the "consistent" rule.

### 4.5 `Comparable` vs `Comparator` (guaranteed question)

| | `Comparable<T>` | `Comparator<T>` |
|---|---|---|
| Where | Implemented **by** the class itself | A **separate** object / lambda |
| Method | `int compareTo(T other)` | `int compare(T a, T b)` |
| Meaning | The class's **natural ordering** | An **alternative / external** ordering |
| How many | One per class | As many as you like |
| Use when | There's one obvious order (dates, IDs) | Sorting the same type many ways, or you can't change the class |

```java
// Natural ordering
public record Incident(Long id, String country, LocalDate date, Severity severity)
        implements Comparable<Incident> {
    @Override public int compareTo(Incident o) { return date.compareTo(o.date); }
}

// External orderings — the ones you will actually use in the exercise
Comparator<Incident> byDate       = Comparator.comparing(Incident::date);
Comparator<Incident> byDateDesc   = Comparator.comparing(Incident::date).reversed();
Comparator<Incident> bySeverity   = Comparator.comparingInt(i -> i.severity().weight());
Comparator<Incident> composite    = Comparator
        .comparing(Incident::severity, Comparator.reverseOrder())   // primary: worst first
        .thenComparing(Incident::date, Comparator.reverseOrder())   // then: newest first
        .thenComparing(Incident::id);                               // tie-break: stable & deterministic
```

**Null-safe comparators (a real edge case they may plant):**
```java
Comparator<Incident> safe = Comparator.comparing(
        Incident::country,
        Comparator.nullsLast(Comparator.naturalOrder()));
```

**Case-insensitive:**
```java
Comparator.comparing(Incident::country, String.CASE_INSENSITIVE_ORDER)
```

**Performance note worth saying:** prefer `comparingInt` / `comparingLong` /
`comparingDouble` over `comparing` for primitives — it avoids boxing.

**The contract trap:** a comparator must be *consistent* (transitive, antisymmetric). The
old `(a, b) -> a.value() - b.value()` trick **overflows** for large ints — use
`Integer.compare(a, b)` instead. If your comparator is inconsistent, `List.sort` can throw
`IllegalArgumentException: Comparison method violates its general contract!`. Naming this
is a strong signal.

**Sort stability:** `Collections.sort` / `List.sort` / `Stream.sorted` are **stable** —
equal elements keep their original relative order. This matters for reproducible top-N
results, but **don't rely on it** — add an explicit tie-break instead.

### 4.6 Manual aggregation with `Map` — the non-stream toolkit

You must be equally fluent without streams. Sometimes a loop is clearer, and sometimes
the interviewer will ask you to "do it without streams".

```java
// Count occurrences — three equivalent idioms
Map<String, Integer> counts = new HashMap<>();
for (Incident i : incidents) {
    counts.merge(i.country(), 1, Integer::sum);                 // BEST: concise + null-safe
    // counts.put(i.country(), counts.getOrDefault(i.country(), 0) + 1);   // also fine
    // counts.compute(i.country(), (k, v) -> v == null ? 1 : v + 1);
}

// Multi-map: group into lists
Map<String, List<Incident>> byCountry = new LinkedHashMap<>();
for (Incident i : incidents) {
    byCountry.computeIfAbsent(i.country(), k -> new ArrayList<>()).add(i);
}
```

**Know the difference (likely follow-up question):**

| Method | Behaviour |
|---|---|
| `put` | Always overwrites. Returns the previous value or `null`. |
| `putIfAbsent` | Only inserts if absent **or currently mapped to null**. |
| `getOrDefault(k, d)` | Read-only; does **not** insert `d`. |
| `computeIfAbsent(k, fn)` | Inserts `fn(k)` only if absent. **Does not call `fn` if present** — that's why it's efficient for multi-maps. |
| `computeIfPresent(k, fn)` | Only when present. Returning `null` from `fn` **removes** the entry. |
| `compute(k, fn)` | Always calls `fn`, with `null` for a missing value. |
| `merge(k, v, fn)` | If absent → `v`; else → `fn(old, v)`. Returning `null` removes the entry. |

### 4.7 Iteration, mutation and `ConcurrentModificationException`

```java
// WRONG — throws ConcurrentModificationException
for (Incident i : incidents) {
    if (i.severity() == Severity.LOW) incidents.remove(i);
}

// RIGHT (1) — Iterator.remove
Iterator<Incident> it = incidents.iterator();
while (it.hasNext()) {
    if (it.next().severity() == Severity.LOW) it.remove();
}

// RIGHT (2) — removeIf, the modern one-liner
incidents.removeIf(i -> i.severity() == Severity.LOW);

// RIGHT (3) — build a new list (best in a service; no mutation of the caller's data)
List<Incident> kept = incidents.stream()
        .filter(i -> i.severity() != Severity.LOW)
        .toList();
```

> **Explain it as:** "Most collections are *fail-fast*. They keep a `modCount`, and the
> iterator checks it on every `next()`. If the collection was structurally modified
> outside the iterator, it throws immediately rather than silently producing wrong
> results."

### 4.8 Immutability and defensive copying (a favourite trap)

```java
List<String> a = List.of("x");            // immutable, throws UnsupportedOperationException on add
List<String> b = Arrays.asList("x", "y"); // FIXED-SIZE view over an array:
                                          //   set(0, "z") works, add()/remove() throw!
List<String> c = Collections.unmodifiableList(inner);  // a VIEW — changes to 'inner' still show through
List<String> d = List.copyOf(inner);      // a true immutable SNAPSHOT
List<String> e = stream.toList();         // unmodifiable (Java 16+)
List<String> f = stream.collect(Collectors.toList());  // modifiable ArrayList (in practice)
```

**Also:** `List.of()` and `Map.of()` **reject `null` elements** with an NPE. If your data
can contain nulls, `Collectors.toList()` / `new ArrayList<>()` are safer.

**Service-layer rule to state:** *"A service method shouldn't mutate the collection it was
handed — the caller doesn't expect it. I return a new collection instead."*

### 4.9 Arrays vs collections

```java
int[] primitives = {3, 1, 2};
Arrays.sort(primitives);                         // in place
int sum = Arrays.stream(primitives).sum();       // IntStream — no boxing

Integer[] boxed = {3, 1, 2};
Arrays.sort(boxed, Comparator.reverseOrder());   // Comparator only works on objects

List<Integer> list = Arrays.stream(primitives).boxed().toList();
int[] back = list.stream().mapToInt(Integer::intValue).toArray();

// TRAP: Arrays.asList on a primitive array
List<int[]> wrong = Arrays.asList(primitives);   // ONE element: the array itself!
```

**Two more array gotchas:**
- `Arrays.equals(a, b)` compares contents; `a.equals(b)` compares references.
  Use `Arrays.deepEquals` for nested arrays, `Arrays.toString` / `deepToString` to print.
- Arrays are **covariant** (`Object[] o = new String[1];` compiles, then
  `o[0] = 1;` throws `ArrayStoreException` at runtime). Generics are **invariant**, which
  is why they're safer.

### 4.10 Generics essentials

```java
// PECS: Producer Extends, Consumer Super
public static double sumSeverity(List<? extends Incident> source) { ... }  // reads from it
public static void addAll(List<? super Incident> target, Incident... xs) { ... } // writes to it

// A generic utility method — realistic in a service
public static <T, K> Map<K, List<T>> groupBy(List<T> items, Function<T, K> keyFn) {
    Map<K, List<T>> out = new LinkedHashMap<>();
    for (T item : items) out.computeIfAbsent(keyFn.apply(item), k -> new ArrayList<>()).add(item);
    return out;
}
```

**Type erasure** — one clean sentence: *"Generics are compile-time only. At runtime
`List<String>` and `List<Integer>` are both just `List`, which is why you can't do
`new T[]` or `instanceof List<String>`, and why you can't overload on `List<String>` vs
`List<Integer>`."*

---

## 5. Streams — filtering and aggregating (the heart of the exercise)

### 5.1 The mental model

A stream is a **pipeline**, not a data structure. It has three parts:

```
SOURCE            INTERMEDIATE (lazy, return a Stream)      TERMINAL (eager, ends it)
list.stream() →   filter / map / sorted / distinct / limit → collect / toList / count / reduce
```

**Facts to state confidently:**
- Intermediate operations are **lazy** — nothing runs until a terminal operation is
  called. `list.stream().filter(...)` on its own does literally nothing.
- The pipeline is executed in a **single pass** where possible, element by element
  (loop fusion) — not one full pass per operation.
- Streams are **single-use**. Reusing one throws `IllegalStateException: stream has
  already been operated upon or closed`.
- Streams **do not modify the source**. They produce new results.
- **Short-circuiting** operations (`findFirst`, `anyMatch`, `limit`) stop early — that's
  why `filter().findFirst()` doesn't scan the whole list.
- Lambdas in a stream should be **stateless and side-effect free**. Mutating an external
  list from inside `forEach` is the classic anti-pattern (`collect` instead).

**Order of operations matters for performance** — a genuinely good thing to say aloud:
```java
// slower: sorts everything, then throws most away
.sorted(cmp).filter(pred).toList()
// faster: shrink first, then sort the smaller set
.filter(pred).sorted(cmp).toList()
// and map after filter, so you don't transform elements you're about to discard
.filter(pred).map(fn).toList()
```

### 5.2 The operations you must know cold

| Operation | Purpose |
|---|---|
| `filter(Predicate)` | Keep matching elements |
| `map(Function)` | Transform each element 1→1 |
| `flatMap(Function)` | Flatten 1→many (e.g. each incident has a `List<String> tags`) |
| `mapToInt/Long/Double` | To a primitive stream — unlocks `sum()`, `average()`, `summaryStatistics()` |
| `boxed()` | Primitive stream → object stream |
| `distinct()` | Deduplicate using `equals`/`hashCode` |
| `sorted()` / `sorted(cmp)` | Natural / custom ordering |
| `limit(n)` / `skip(n)` | Top-N / pagination |
| `peek(Consumer)` | Debugging only — never for side effects in real code |
| `takeWhile` / `dropWhile` | Java 9+; prefix-based cutting on *sorted* data |
| `anyMatch/allMatch/noneMatch` | Boolean checks, short-circuiting |
| `findFirst` / `findAny` | `Optional<T>` |
| `count()` | `long` |
| `min(cmp)` / `max(cmp)` | `Optional<T>` |
| `reduce` | Fold to a single value |
| `collect(Collector)` | The general-purpose accumulator |
| `toList()` | Java 16+, unmodifiable list |

**`allMatch` on an empty stream returns `true`** (vacuous truth) and `anyMatch` returns
`false`. That's a real edge case — know it.

### 5.3 Filtering — patterns

```java
// Simple predicate
incidents.stream().filter(i -> i.country().equals(country)).toList();

// Null-safe: guard BEFORE dereferencing
incidents.stream()
        .filter(Objects::nonNull)
        .filter(i -> country.equalsIgnoreCase(i.country()))   // constant first: null-safe
        .toList();

// Combining predicates — readable and reusable
Predicate<Incident> isCritical = i -> i.severity() == Severity.CRITICAL;
Predicate<Incident> isRecent   = i -> i.date().isAfter(LocalDate.now().minusDays(30));
var criticalAndRecent = incidents.stream().filter(isCritical.and(isRecent)).toList();
var notCritical       = incidents.stream().filter(isCritical.negate()).toList();

// Optional/blank-tolerant filtering (very common in "search" style steps)
public List<Incident> search(List<Incident> in, String country, Severity sev, LocalDate from) {
    return in.stream()
            .filter(i -> country == null || country.equalsIgnoreCase(i.country()))
            .filter(i -> sev == null     || i.severity() == sev)
            .filter(i -> from == null    || !i.date().isBefore(from))
            .toList();
}
```
> That last one is the "optional filter" idiom: a null criterion means "don't filter on
> this". Worth calling out — it's exactly how a real search service behaves.

**Deduplicate by a key (no `distinctBy` in Java — know the two idioms):**
```java
// (a) via a Set of seen keys — order preserving, one pass
Set<String> seen = new HashSet<>();
List<Incident> unique = incidents.stream()
        .filter(i -> seen.add(i.country()))   // add() returns false if already present
        .toList();

// (b) via toMap keeping the first (or last) per key
Collection<Incident> uniqueByCountry = incidents.stream()
        .collect(Collectors.toMap(Incident::country, i -> i,
                (first, second) -> first,      // merge: keep the first
                LinkedHashMap::new))
        .values();
```
*(Idiom (a) is stateful and technically unsafe in a parallel stream — say so if you use it.)*

### 5.4 Aggregating — the essential `Collectors`

```java
import static java.util.stream.Collectors.*;

// COUNT PER GROUP  →  Map<String, Long>
Map<String, Long> perCountry = incidents.stream()
        .collect(groupingBy(Incident::country, counting()));

// COUNT PER GROUP, deterministic order  →  TreeMap sorted by country name
Map<String, Long> sortedPerCountry = incidents.stream()
        .collect(groupingBy(Incident::country, TreeMap::new, counting()));

// SUM PER GROUP
Map<String, Integer> casualtiesPerCountry = incidents.stream()
        .collect(groupingBy(Incident::country, summingInt(Incident::casualties)));

// AVERAGE PER GROUP  (note: returns Double, and an empty group can't occur here)
Map<String, Double> avgSeverity = incidents.stream()
        .collect(groupingBy(Incident::country,
                 averagingInt(i -> i.severity().weight())));

// FULL STATS PER GROUP — count, sum, min, max, average in ONE pass
Map<String, IntSummaryStatistics> stats = incidents.stream()
        .collect(groupingBy(Incident::country, summarizingInt(Incident::casualties)));
stats.get("Mali").getMax();

// GROUP INTO LISTS (the default downstream)
Map<String, List<Incident>> grouped = incidents.stream()
        .collect(groupingBy(Incident::country));

// GROUP AND TRANSFORM the members
Map<String, List<String>> typesPerCountry = incidents.stream()
        .collect(groupingBy(Incident::country, mapping(Incident::type, toList())));

// GROUP INTO A SET (dedupes)
Map<String, Set<String>> distinctTypes = incidents.stream()
        .collect(groupingBy(Incident::country, mapping(Incident::type, toSet())));

// GROUP AND FILTER MEMBERS — note filtering() KEEPS empty groups, a plain
// stream filter before groupingBy DROPS them. This distinction is interview gold.
Map<String, List<Incident>> criticalPerCountry = incidents.stream()
        .collect(groupingBy(Incident::country,
                 filtering(i -> i.severity() == Severity.CRITICAL, toList())));

// GROUP AND FLATTEN (each incident has List<String> tags)
Map<String, Set<String>> tagsPerCountry = incidents.stream()
        .collect(groupingBy(Incident::country,
                 flatMapping(i -> i.tags().stream(), toSet())));

// GROUP AND PICK THE MAX PER GROUP → Optional inside the map
Map<String, Optional<Incident>> worst = incidents.stream()
        .collect(groupingBy(Incident::country,
                 maxBy(Comparator.comparingInt(i -> i.severity().weight()))));

// GROUP AND PICK THE MAX, unwrapped — collectingAndThen removes the Optional
Map<String, Incident> worstClean = incidents.stream()
        .collect(groupingBy(Incident::country,
                 collectingAndThen(
                     maxBy(Comparator.comparingInt(i -> i.severity().weight())),
                     Optional::orElseThrow)));

// PARTITION — exactly two keys: true and false, and BOTH are always present
Map<Boolean, List<Incident>> split = incidents.stream()
        .collect(partitioningBy(i -> i.severity().weight() >= 3));

// JOIN STRINGS
String countryList = incidents.stream()
        .map(Incident::country).distinct().sorted()
        .collect(joining(", ", "[", "]"));

// TO MAP — id → incident. THROWS IllegalStateException on duplicate keys unless
// you supply a merge function. This is the #1 toMap trap.
Map<Long, Incident> byId = incidents.stream()
        .collect(toMap(Incident::id, i -> i));
Map<String, Integer> totals = incidents.stream()
        .collect(toMap(Incident::country, Incident::casualties, Integer::sum));  // safe

// NESTED / TWO-LEVEL GROUPING
Map<String, Map<Severity, Long>> perCountryPerSeverity = incidents.stream()
        .collect(groupingBy(Incident::country,
                 groupingBy(Incident::severity, counting())));

// TEEING (Java 12) — two collectors, one pass, merged result
record MinMax(Optional<Incident> earliest, Optional<Incident> latest) {}
MinMax range = incidents.stream().collect(teeing(
        minBy(Comparator.comparing(Incident::date)),
        maxBy(Comparator.comparing(Incident::date)),
        MinMax::new));
```

**`toMap` also has another trap:** its merge function is only called on collision, and
`Collectors.toMap` throws a **`NullPointerException` if a *value* is null** (because it
uses `Map.merge` internally). If values may be null, group manually.

### 5.5 Primitive streams and statistics

```java
IntSummaryStatistics s = incidents.stream().mapToInt(Incident::casualties).summaryStatistics();
s.getCount(); s.getSum(); s.getMin(); s.getMax(); s.getAverage();

// average() returns OptionalDouble — an EMPTY list has no average.
double avg = incidents.stream().mapToInt(Incident::casualties).average().orElse(0.0);

// sum() on an empty stream is 0 — that one is safe.
long total = incidents.stream().mapToInt(Incident::casualties).sum();

// max() returns OptionalInt
int worst = incidents.stream().mapToInt(Incident::casualties).max().orElse(0);
```
> **Say this:** *"`average()` returns an `OptionalDouble` precisely because the average of
> an empty set is undefined — the API is forcing me to decide what to do. I'll return 0.0
> here, but returning `Optional` to the caller would also be valid."*

### 5.6 `reduce` — and when to prefer it

```java
// reduce with identity — always returns a value, never Optional
int total = incidents.stream().reduce(0, (acc, i) -> acc + i.casualties(), Integer::sum);

// reduce without identity — returns Optional because an empty stream has no result
Optional<Incident> worst = incidents.stream()
        .reduce((a, b) -> a.casualties() >= b.casualties() ? a : b);

// Three-arg reduce: identity, accumulator, combiner (combiner only used in parallel)
```
**Identity must be a true identity** (`f(identity, x) == x`) and the accumulator must be
**associative**, otherwise parallel results are wrong. Prefer `mapToInt(...).sum()` over
`reduce` for plain sums — it's clearer and avoids boxing. Use `reduce` when the fold is
genuinely custom.

**Never do this** (mutable accumulation with `reduce` — quadratic and parallel-unsafe):
```java
list.stream().reduce("", (a, b) -> a + b);   // O(n²) string building
list.stream().collect(joining());            // correct: uses StringBuilder internally
```

### 5.7 Multi-key grouping — three approaches

```java
// (1) Local record key — CLEANEST and most modern. equals/hashCode are free.
record Key(String country, Severity severity) {}
Map<Key, Long> counts = incidents.stream()
        .collect(groupingBy(i -> new Key(i.country(), i.severity()), counting()));

// (2) Nested groupingBy — good when you actually want a nested structure
Map<String, Map<Severity, Long>> nested = incidents.stream()
        .collect(groupingBy(Incident::country, groupingBy(Incident::severity, counting())));

// (3) String concatenation key — WORKS but is fragile ("Mali|HIGH"). Mention that you
//     avoid it because of separator collisions and loss of type safety.
```

### 5.8 Top-N / ranking — the pattern they will almost certainly ask for

```java
/** Top N countries by incident count, ties broken alphabetically, deterministic. */
public List<String> topCountries(List<Incident> incidents, int n) {
    if (incidents == null || incidents.isEmpty() || n <= 0) return List.of();

    return incidents.stream()
            .filter(Objects::nonNull)
            .map(Incident::country)
            .filter(Objects::nonNull)
            .collect(groupingBy(Function.identity(), counting()))   // Map<String, Long>
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed()   // count desc
                    .thenComparing(Map.Entry.comparingByKey()))             // TIE-BREAK: name asc
            .limit(n)
            .map(Map.Entry::getKey)
            .toList();
}
```
**Say all of this out loud while writing it:**
- "I group to get counts, then stream the entry set to rank them."
- "I add an explicit tie-break so the result is deterministic — otherwise two countries
  with the same count could come out in any order and the test would be flaky."
- "`limit(n)` after sorting gives me the top N; if fewer exist I just get fewer, which I
  think is the right behaviour — shall I confirm that?"
- "This is O(n) to group plus O(k log k) to sort the distinct keys. If the number of
  distinct keys were huge I'd use a size-N `PriorityQueue` for O(k log n) instead."

**Sorting a map by value into an ordered map:**
```java
LinkedHashMap<String, Long> ranked = counts.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                       (a, b) -> a, LinkedHashMap::new));   // LinkedHashMap PRESERVES the sort!
```
> **Critical point:** a `HashMap` has no order, so collecting a sorted stream into a plain
> `toMap` **throws the ordering away**. You must supply `LinkedHashMap::new`. This is one
> of the most commonly missed details in interviews — call it out explicitly.

### 5.9 `Optional` — use it properly

```java
// GOOD
public Optional<Incident> findById(Long id) {
    return incidents.stream().filter(i -> Objects.equals(i.id(), id)).findFirst();
}

Optional<String> country = findById(1L).map(Incident::country);
String name  = country.orElse("Unknown");
String name2 = country.orElseGet(() -> expensiveDefault());   // lazy — only if empty
Incident i   = findById(1L).orElseThrow(() -> new IncidentNotFoundException(1L));
findById(1L).ifPresentOrElse(this::process, () -> log.warn("not found"));

// Java 9+: turn an Optional into a 0-or-1 element stream — great for flatMap
List<Incident> found = ids.stream().map(this::findById).flatMap(Optional::stream).toList();
```

**Anti-patterns to name:**
- `if (opt.isPresent()) opt.get()` — that's just a null check with extra steps.
- `opt.get()` without a check — throws `NoSuchElementException`.
- `Optional` as a **field**, a **method parameter**, or in an **entity** — it's not
  `Serializable` and it adds noise. It's designed as a **return type**.
- `Optional<List<T>>` — return an **empty list** instead. "Empty collection, never null,
  never `Optional`."
- `orElse(expensive())` — the argument is evaluated eagerly even when the value is
  present. Use `orElseGet`.

### 5.10 Parallel streams — know when to say no

```java
list.parallelStream()...     // or .stream().parallel()
```
**Say:** *"I wouldn't reach for parallel streams here. They only pay off for large
datasets with CPU-bound, stateless, associative operations on a splittable source like an
`ArrayList`. For a few thousand records the fork/join overhead usually makes it slower,
and it introduces ordering and thread-safety concerns — for example collecting into a
plain `HashMap` or `ArrayList` from multiple threads is unsafe. I'd measure before
parallelising."*

Also: parallel streams share the **common ForkJoinPool** by default, so one slow parallel
stream can starve others in the same JVM.

### 5.11 Functional interfaces cheat sheet

| Interface | Signature | Typical use |
|---|---|---|
| `Predicate<T>` | `boolean test(T)` | `filter` |
| `Function<T,R>` | `R apply(T)` | `map` |
| `BiFunction<T,U,R>` | `R apply(T,U)` | `merge`, `reduce` |
| `Consumer<T>` | `void accept(T)` | `forEach` |
| `Supplier<T>` | `T get()` | `orElseGet`, collection factories |
| `UnaryOperator<T>` | `T apply(T)` | `replaceAll` |
| `BinaryOperator<T>` | `T apply(T,T)` | `reduce`, `toMap` merge |
| `Comparator<T>` | `int compare(T,T)` | `sorted` |

**Method references — the four kinds** (a nice, precise thing to be able to list):
1. Static — `Integer::parseInt`
2. Instance of a *particular* object — `System.out::println`
3. Instance of an *arbitrary* object of a type — `String::toLowerCase`, `Incident::country`
4. Constructor — `ArrayList::new`, `Incident::new`

**Closures/effectively final:** a lambda can only capture local variables that are
effectively final. That's why you can't increment a local counter inside a lambda — and
the right answer is `count()`, a collector, or an `AtomicInteger` if you truly must.

---

## 6. "Light algorithmic thinking" — the patterns that actually show up

He said *light*. That means no dynamic programming, no graph theory. It means: can you
pick a data structure that turns an O(n²) loop into an O(n) pass, and can you reason
about correctness on edge cases. Here are the patterns worth having in your fingers.

### 6.1 Frequency map (by far the most likely)

```java
Map<String, Integer> freq = new HashMap<>();
for (String s : items) freq.merge(s, 1, Integer::sum);
// or: items.stream().collect(groupingBy(identity(), counting()))
```
**Uses:** most common value, duplicates, anagram checks, "which country reports most".
**Complexity:** O(n) time, O(k) space where k = distinct keys.

### 6.2 Set for O(1) membership — killing a nested loop

```java
// BEFORE: O(n*m)
for (var a : listA) if (listB.contains(a)) result.add(a);

// AFTER: O(n+m)
Set<String> bSet = new HashSet<>(listB);
for (var a : listA) if (bSet.contains(a)) result.add(a);
```
Set operations you should name: `retainAll` (intersection), `removeAll` (difference),
`addAll` (union) — all much faster on a `HashSet` than a `List`.

### 6.3 Sorting + single pass (greedy)

Many "light algorithm" questions become trivial once sorted. **Always ask yourself: does
sorting first make this a one-pass problem?**

Examples: merging overlapping ranges, finding the largest gap, detecting a schedule
conflict, computing consecutive streaks.

### 6.4 Merging overlapping intervals (very plausible for a safety/travel domain)

```java
public List<DateRange> merge(List<DateRange> ranges) {
    if (ranges == null || ranges.isEmpty()) return List.of();

    List<DateRange> sorted = ranges.stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(DateRange::start))
            .collect(Collectors.toCollection(ArrayList::new));

    List<DateRange> merged = new ArrayList<>();
    DateRange current = sorted.get(0);
    for (int i = 1; i < sorted.size(); i++) {
        DateRange next = sorted.get(i);
        // touching or overlapping — note: !next.start().isAfter(current.end())
        // treats adjacent ranges as mergeable. Ask whether that's wanted!
        if (!next.start().isAfter(current.end())) {
            LocalDate newEnd = current.end().isAfter(next.end()) ? current.end() : next.end();
            current = new DateRange(current.start(), newEnd);
        } else {
            merged.add(current);
            current = next;
        }
    }
    merged.add(current);          // don't forget the last one — the classic off-by-one bug
    return List.copyOf(merged);
}
```
**Edge cases to name aloud:** empty input; a single range; fully-contained ranges
(`[1–10]` and `[3–4]`); exactly-adjacent ranges (`[1–5]`, `[6–10]` — mergeable or not?);
identical ranges; inverted ranges where `end < start`.
**Complexity:** O(n log n) for the sort, O(n) for the sweep.

### 6.5 Longest streak / consecutive run

```java
/** Longest run of consecutive days that had at least one incident. */
public int longestConsecutiveDayStreak(List<Incident> incidents) {
    if (incidents == null || incidents.isEmpty()) return 0;

    Set<LocalDate> days = incidents.stream()
            .map(Incident::date).filter(Objects::nonNull)
            .collect(Collectors.toSet());          // dedupe: several incidents per day

    int best = 0;
    for (LocalDate d : days) {
        if (days.contains(d.minusDays(1))) continue;   // only start counting at a run's beginning
        int len = 1;
        LocalDate cur = d;
        while (days.contains(cur.plusDays(1))) { cur = cur.plusDays(1); len++; }
        best = Math.max(best, len);
    }
    return best;
}
```
> **The trick worth explaining:** the `continue` guard means every element is visited by
> at most one inner walk, so despite the nested loop this is **O(n)**, not O(n²). Being
> able to justify that is exactly the "thought process" they want to hear.

### 6.6 Sliding window / running totals

```java
/** 7-day rolling total of incidents, keyed by the window's end date. */
public Map<LocalDate, Long> rollingSevenDay(List<Incident> incidents, LocalDate from, LocalDate to) {
    Map<LocalDate, Long> perDay = incidents.stream()
            .collect(groupingBy(Incident::date, counting()));

    Map<LocalDate, Long> out = new LinkedHashMap<>();
    for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
        long sum = 0;
        for (int k = 0; k < 7; k++) sum += perDay.getOrDefault(d.minusDays(k), 0L);
        out.put(d, sum);
    }
    return out;
}
```
> Mention the optimisation: *"This is O(days × 7). A true sliding window that adds the new
> day and subtracts the day falling out would be O(days). With a 7-day window the constant
> is tiny, so I'd keep the readable version unless the range were huge."* — **that
> trade-off sentence is worth more than the optimisation itself.**

### 6.7 Top-K with a heap

```java
public List<Incident> topK(List<Incident> incidents, int k) {
    if (incidents == null || k <= 0) return List.of();
    // Min-heap of size k: the smallest of the current best-k sits at the head.
    PriorityQueue<Incident> heap =
            new PriorityQueue<>(Comparator.comparingInt(Incident::casualties));
    for (Incident i : incidents) {
        heap.offer(i);
        if (heap.size() > k) heap.poll();       // evict the weakest
    }
    List<Incident> out = new ArrayList<>(heap);
    out.sort(Comparator.comparingInt(Incident::casualties).reversed());
    return out;
}
```
**O(n log k)** vs **O(n log n)** for a full sort. Only worth it when n is large and k is
small — say that trade-off rather than reaching for it automatically.

### 6.8 Two pointers

Useful on **sorted** data: find a pair summing to a target, compare two sorted lists,
merge two sorted sequences.
```java
int lo = 0, hi = arr.length - 1;
while (lo < hi) {
    int sum = arr[lo] + arr[hi];
    if (sum == target) return new int[]{lo, hi};
    if (sum < target) lo++; else hi--;
}
```

### 6.9 Prefix sums

Precompute cumulative totals so any range sum is O(1):
```java
long[] prefix = new long[n + 1];
for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + values[i];
long rangeSum = prefix[hi + 1] - prefix[lo];   // inclusive [lo, hi]
```
Useful if asked for "total casualties between two dates" repeatedly.

### 6.10 Recursion / simple traversal

If the domain has a hierarchy (region → country → office), be ready for a small
depth-first walk:
```java
private void collect(Node node, List<Node> out) {
    if (node == null) return;
    out.add(node);
    for (Node child : node.children()) collect(child, out);
}
```
Mention: base case first, watch for cycles (keep a `visited` `Set`), and that deep
recursion risks `StackOverflowError` — an explicit `ArrayDeque` stack is the iterative
alternative.

### 6.11 Big-O talking points

- **State the complexity of every solution you write, unprompted.** One short sentence:
  *"That's O(n) time, O(k) extra space for the map."*
- Common ones: hash lookup O(1); sort O(n log n); nested loop O(n²); binary search
  O(log n); heap push/pop O(log n).
- **Don't optimise prematurely.** The right framing is: *"Correct and readable first.
  Here's where it would break down at scale, and here's what I'd change."*

---

## 7. Spring Boot: how services and repositories fit together

He said you *won't* build endpoints — but you need to not be lost in the project. Here is
exactly the amount you need, plus the follow-up questions that could reasonably come.

### 7.1 The layered picture

```
(Controller — NOT part of this exercise)
        ↓  calls
   @Service          ← YOU WRITE HERE. Business logic, orchestration, transactions.
        ↓  calls
  @Repository        ← Data access only. Given to you, probably in-memory or Spring Data JPA.
        ↓
    Database / in-memory list
```

**Why each layer exists — say this in one breath:**
> *"The controller translates HTTP into Java and back — it should be thin. The service
> holds the business rules and is the transaction boundary; it's the layer I can unit test
> without any web or database infrastructure. The repository only knows how to read and
> write data. Keeping them separate means I can change the database or the API shape
> without touching the business rules, and it makes each layer independently testable."*

**Why business logic must not live in the controller:**
> *"It can't be reused by a scheduled job or another entry point, it can only be tested by
> spinning up the web layer, and it couples business rules to HTTP concerns like status
> codes and JSON. It also tends to grow into a class nobody can safely change."*

### 7.2 The annotations, and what Spring actually does

```java
@Service                                       // 1. component-scanned, registered as a bean
public class IncidentService {

    private final IncidentRepository repository;   // 2. final = immutable dependency

    public IncidentService(IncidentRepository repository) {   // 3. constructor injection
        this.repository = repository;
    }
}
```

**What happens at startup, step by step (a strong answer if asked):**
1. `@SpringBootApplication` triggers **component scanning** from its own package downward.
2. Spring finds classes annotated with `@Component` and its specialisations —
   `@Service`, `@Repository`, `@Controller`, `@Configuration`.
3. For each, it creates a **bean definition** (a recipe: class, scope, dependencies).
4. It builds the **ApplicationContext** (the IoC container) and instantiates the beans,
   resolving each constructor's parameters by looking up matching beans — that's
   **dependency injection**.
5. Beans are **singleton-scoped by default**, one instance per context.
6. `@Repository` additionally enables **exception translation** — vendor-specific
   `SQLException`s become Spring's `DataAccessException` hierarchy.

**Inversion of Control in one sentence:** *"I don't create my dependencies with `new`;
the framework creates them and hands them to me. That inverts the control of object
creation, which is what makes the class easy to test — I can just pass a mock into the
constructor."*

**Why constructor injection over `@Autowired` on a field:**
- Dependencies can be `final` → genuinely immutable, thread-safe.
- The object is never in a half-built state.
- **Testable without Spring** — `new IncidentService(mockRepo)`.
- Compilation fails if a required dependency is missing, rather than an NPE at runtime.
- A constructor with too many parameters *visibly* signals the class does too much.
- Since Spring 4.3, a single constructor needs **no `@Autowired` annotation at all**.

**`@Component` vs `@Service` vs `@Repository`:** technically all beans; the distinction is
**semantic intent** (plus exception translation for `@Repository`, and AOP pointcut
targeting). Saying "they're the same to the container but they document the layer" is the
correct, honest answer.

### 7.3 Spring Data repositories — what you'll likely be handed

```java
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByCountry(String country);                       // derived query
    List<Incident> findByCountryAndSeverity(String c, Severity s);
    List<Incident> findByDateBetween(LocalDate from, LocalDate to);
    List<Incident> findByCountryOrderByDateDesc(String country);
    long countByCountry(String country);
    Optional<Incident> findFirstBySeverityOrderByDateDesc(Severity s);
    Page<Incident> findByCountry(String country, Pageable pageable);

    @Query("SELECT i.country, COUNT(i) FROM Incident i GROUP BY i.country")
    List<Object[]> countPerCountry();                                   // JPQL
}
```
**How it works (one sentence):** *"Spring Data creates a **dynamic proxy** implementing
the interface at startup. It parses the method name into a query, or uses the `@Query`
you gave it, and delegates to the JPA `EntityManager`, which Hibernate turns into SQL."*

**JPA vs Hibernate:** *"JPA is the specification — the interfaces and annotations.
Hibernate is the most common implementation of that specification. I code against JPA so
the implementation is swappable."*

**Important for this exercise:** the repository might be a plain in-memory class, not JPA
at all. **Look at it before assuming.** If it just returns a `List`, all the aggregation
happens in your service in Java — which is exactly why the exercise is collections-focused.

**Good thing to say:** *"For a real reporting query I'd normally push the aggregation into
the database with a `GROUP BY`, because the DB is far better at it and I avoid loading
everything into memory. Here I'll do it in Java since that's the point of the exercise —
but that's the trade-off I'd flag in a code review."*

### 7.4 `@Transactional` — the sentences that matter

```java
@Transactional(readOnly = true)      // on a query method: no dirty-checking, a real optimisation
public List<Incident> findAll() { ... }

@Transactional                       // on a write method: all-or-nothing
public Incident create(Incident i) { ... }
```
- Puts the method inside a **database transaction**: commit on normal return, **rollback
  on an unchecked exception** by default. Checked exceptions **do not** roll back unless
  you add `rollbackFor`.
- Implemented with an **AOP proxy**, which leads to the classic trap:
  **self-invocation doesn't work** — calling `this.otherTransactionalMethod()` bypasses
  the proxy, so the annotation has no effect.
- Also requires the method to be **public** for the default proxy mode.
- The **service** is the right transaction boundary because one business operation may
  touch several repositories and must succeed or fail as a unit.

### 7.5 Configuration you may see

- `application.properties` / `application.yml` — external configuration.
- `@Value("${app.threshold}")` for a single property; `@ConfigurationProperties` for a
  typed group. **Profiles** (`application-dev.yml`, `@Profile("dev")`) separate
  environments.
- `@Configuration` + `@Bean` — for beans you can't annotate (third-party classes).

### 7.6 Testing the service (they may ask you to add a test)

```java
@ExtendWith(MockitoExtension.class)         // plain JUnit 5 + Mockito — NO Spring context, fast
class IncidentServiceTest {

    @Mock  IncidentRepository repository;
    @InjectMocks IncidentService service;

    @Test
    void countsIncidentsPerCountry() {
        when(repository.findAll()).thenReturn(List.of(
                incident("Mali", Severity.HIGH),
                incident("Mali", Severity.LOW),
                incident("Chad", Severity.HIGH)));

        Map<String, Long> result = service.countPerCountry();

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of("Mali", 2L, "Chad", 1L));
        verify(repository).findAll();
    }

    @Test
    void returnsEmptyMapWhenNoIncidents() {
        when(repository.findAll()).thenReturn(List.of());
        assertThat(service.countPerCountry()).isEmpty();
    }
}
```
**Talking points:**
- **Unit test** = one class in isolation, dependencies mocked, milliseconds, tests
  *business logic*. **Integration test** (`@SpringBootTest`) = real wiring/DB, slower,
  tests that the pieces fit together. **Test the aggregation logic as a unit test.**
- **Mock** when the dependency is slow, external, or nondeterministic. Use the **real
  thing** when it's cheap and the interaction is the point (e.g. a real `List`).
- Prefer asserting on the **returned value** over `verify()` on interactions —
  over-verifying makes tests brittle.
- **Name the edge-case tests you'd write** even if you don't write them: empty input,
  nulls, ties, single element, all-same-key.

---

## 8. The edge-case master checklist

**Run this list in your head at the start of every step, and say the relevant ones out
loud.** Even naming an edge case you then choose not to handle scores points — it proves
you saw it.

### 8.1 Input edge cases

| Case | What to do / say |
|---|---|
| **`null` collection argument** | `if (list == null) return List.of();` or `Objects.requireNonNull`. **Ask which they prefer.** |
| **Empty collection** | Must not crash. Usually return empty result / 0 / `Optional.empty()`. |
| **Single element** | Off-by-one killer in interval and streak logic. Test it. |
| **All elements identical** | Grouping produces one key; top-N returns one entry. |
| **`null` elements inside the list** | `.filter(Objects::nonNull)` before dereferencing. |
| **`null` fields** (country, date) | Filter them out, or bucket them under `"UNKNOWN"` — **ask which**. `groupingBy` **throws NPE on a null key**. |
| **Duplicates** | Do they count once or many times? Dedupe by ID? |
| **Whitespace / casing** | `" Mali "` vs `"mali"` vs `"Mali"` — `strip()` and compare case-insensitively if the domain says they're the same. |
| **n <= 0 for top-N** | Return empty list rather than throwing — but say you asked. |
| **n larger than the data** | Return everything you have. |
| **Unsorted input** | Never assume order; sort explicitly if you depend on it. |
| **Very large input** | Mention the complexity and whether a second pass matters. |

### 8.2 Numeric edge cases

| Case | Handling |
|---|---|
| **Division by zero / average of empty** | `average()` gives `OptionalDouble` — `.orElse(0.0)`. Integer `/ 0` throws `ArithmeticException`; `double / 0` gives `Infinity`. |
| **Integer overflow** | `int` sums of large data → use `long` or `mapToLong`. `a - b` in comparators overflows; use `Integer.compare`. `Math.addExact` throws instead of wrapping. |
| **Floating-point precision** | `0.1 + 0.2 != 0.3`. Never use `==` on doubles; compare with an epsilon. **For money, use `BigDecimal`** — and `BigDecimal.equals` compares scale (`2.0` != `2.00`), so use `compareTo`. |
| **`NaN`** | `Double.NaN != Double.NaN`. Use `Double.isNaN()`. `NaN` also poisons sorts and `max`. |
| **Rounding** | State the rule: `Math.round`, `BigDecimal.setScale(2, RoundingMode.HALF_UP)`. |
| **Percentages** | `(count * 100.0) / total` — the `.0` forces double arithmetic. Guard `total == 0`. |
| **Negative values** | Are negative casualties/severities possible? Should they be rejected? |

### 8.3 String edge cases

- `null` vs `""` vs `"   "` → `isBlank()` covers the last two, but **not null**.
- `equals` vs `equalsIgnoreCase`; put the constant first (`"Mali".equals(x)`) to be
  null-safe.
- `==` compares references, **except** that string literals are interned in the **string
  pool**, which is why `"a" == "a"` is `true` but `new String("a") == "a"` is `false`.
  **Always use `.equals`.**
- Building strings in a loop with `+` is O(n²) — use `StringBuilder` or
  `Collectors.joining`.
- `split(",")` on `"a,,b"` gives an empty middle element; trailing empties are dropped
  unless you pass a negative limit: `split(",", -1)`.
- Locale-sensitive `toUpperCase()` (the Turkish 'i' problem) — use
  `toUpperCase(Locale.ROOT)` for machine comparisons.

### 8.4 Date/time edge cases (highly relevant for incident tracking)

- **Inclusive vs exclusive** end dates — **always ask**. `isBefore`/`isAfter` are strict;
  "on or after" is `!d.isBefore(from)`.
- Time zones: `LocalDate` has none, `Instant` is UTC, `ZonedDateTime` has one. INSO is
  global, so *"which timezone are these timestamps in?"* is a genuinely great question.
- Month/year boundaries; leap years (`LocalDate.of(2024,2,29)`); DST.
- Future-dated records, and records with a `null` date.
- `ChronoUnit.DAYS.between(a, b)` is **exclusive of the end** — add 1 for an inclusive
  day count.
- `YearMonth.from(date)` is the clean way to bucket by month; it sorts naturally, unlike
  the string `"2024-1"`.

```java
// Inclusive range filter, null-safe
boolean inRange(LocalDate d, LocalDate from, LocalDate to) {
    return d != null && !d.isBefore(from) && !d.isAfter(to);
}

// Group by month, sorted, with zero-filled gaps — a very likely sub-task
Map<YearMonth, Long> perMonth = incidents.stream()
        .filter(i -> i.date() != null)
        .collect(groupingBy(i -> YearMonth.from(i.date()), TreeMap::new, counting()));

// Zero-fill months with no incidents — the requirement people forget!
Map<YearMonth, Long> filled = new LinkedHashMap<>();
for (YearMonth m = start; !m.isAfter(end); m = m.plusMonths(1)) {
    filled.put(m, perMonth.getOrDefault(m, 0L));
}
```
> **Zero-filling is a classic hidden requirement:** "incidents per month" over a chart
> needs months with zero incidents to appear. `groupingBy` alone can never produce a key
> for data that doesn't exist. **Raise this proactively** — it's a great signal.

### 8.5 Output edge cases

- Return **empty collections, never `null`**. Say why: *"it removes null checks from
  every caller."*
- Deterministic ordering — `LinkedHashMap`/`TreeMap`, and explicit tie-breaks.
- Mutability of the returned collection — `List.copyOf` / `toList()` if it shouldn't be
  changed.
- Don't leak internal mutable state (return a copy of an internal list, not the list).
- Don't mutate the caller's input collection.

### 8.6 Exception-handling edge cases

- **Checked vs unchecked:** checked (`Exception`) must be declared or caught — for
  recoverable conditions; unchecked (`RuntimeException`) for programming errors and
  business-rule violations. Spring's ecosystem is unchecked-first, and `@Transactional`
  only rolls back on unchecked by default.
- Prefer a **specific custom exception** (`IncidentNotFoundException extends
  RuntimeException`) over a generic one.
- **Never swallow** an exception (`catch (Exception e) {}`). Never `catch (Throwable)`.
- `finally` **always runs** (barring `System.exit`/JVM death) — and a `return` in
  `finally` silently discards an in-flight exception. Never do it.
- **try-with-resources** for anything `AutoCloseable`; it closes in reverse order and
  handles suppressed exceptions properly. Much safer than a `finally` block.
- Validate **at the boundary** (start of the service method), fail fast with a clear
  message, and don't repeat the validation deeper down.

```java
public List<Incident> byCountry(String country) {
    if (country == null || country.isBlank()) {
        throw new IllegalArgumentException("country must not be blank");
    }
    ...
}
```

### 8.7 Concurrency (only if they probe)

- `HashMap` is **not** thread-safe; `ConcurrentHashMap` is (and rejects null keys/values).
- `Collections.synchronizedList` locks each method but **compound operations
  (check-then-act) are still unsafe** and iteration needs external synchronisation.
- `CopyOnWriteArrayList` for read-heavy, write-rare.
- `AtomicInteger`/`LongAdder` for counters.
- Spring singleton beans are shared across threads → **keep services stateless**; put
  mutable state in local variables, never in fields. **This is a great point to make
  unprompted when writing a service.**

---

## 9. Worked practice problems (INSO-flavoured)

Work through these **with a timer** before the interview. Write them in `Backend/core` or
a scratch project and run them. The domain is deliberately close to what INSO does.

### The domain

```java
public enum Severity { LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);
    private final int weight;
    Severity(int w) { this.weight = w; }
    public int weight() { return weight; }
}

public record Incident(Long id, String country, String region, String type,
                       Severity severity, LocalDate date, int casualties,
                       List<String> tags, Long reportedByNgoId) {}

public record Ngo(Long id, String name, String country, boolean active) {}
```

---

**Q1 — Filter.** Return all incidents for a country, case-insensitively, newest first.

```java
public List<Incident> findByCountry(List<Incident> incidents, String country) {
    if (incidents == null || country == null || country.isBlank()) return List.of();
    return incidents.stream()
            .filter(Objects::nonNull)
            .filter(i -> country.strip().equalsIgnoreCase(i.country()))
            .sorted(Comparator.comparing(Incident::date,
                    Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
}
```
*Talk about:* null/blank guard, case-insensitive match, null-safe date comparator,
returning an unmodifiable list, O(n log n).

---

**Q2 — Count per group.** Number of incidents per country, sorted by country name.

```java
public Map<String, Long> countPerCountry(List<Incident> incidents) {
    if (incidents == null) return Map.of();
    return incidents.stream()
            .filter(i -> i != null && i.country() != null)
            .collect(groupingBy(Incident::country, TreeMap::new, counting()));
}
```
*Talk about:* why `TreeMap` (deterministic, sorted output); why null keys must be filtered
(`groupingBy` throws NPE on a null key); could bucket nulls as `"UNKNOWN"` instead — ask.

---

**Q3 — Sum + average per group in one pass.**

```java
public Map<String, IntSummaryStatistics> casualtyStatsPerCountry(List<Incident> incidents) {
    return incidents.stream()
            .filter(i -> i != null && i.country() != null)
            .collect(groupingBy(Incident::country,
                     summarizingInt(Incident::casualties)));
}
```
*Talk about:* `summarizingInt` gives count/sum/min/max/average in **one** traversal
instead of four separate streams.

---

**Q4 — Top N with tie-breaking.** (See §5.8 for the full solution and the script.)

---

**Q5 — Two-level grouping.** Incidents per country, broken down by severity.

```java
public Map<String, Map<Severity, Long>> perCountryPerSeverity(List<Incident> incidents) {
    return incidents.stream()
            .filter(i -> i != null && i.country() != null && i.severity() != null)
            .collect(groupingBy(Incident::country, TreeMap::new,
                     groupingBy(Incident::severity,
                               () -> new EnumMap<>(Severity.class),
                               counting())));
}
```
*Talk about:* `EnumMap` for the inner map (fast, ordered by declaration), and that severity
levels with zero incidents will be **absent** — zero-fill if the caller needs all four.

---

**Q6 — The worst incident per country, unwrapped.**

```java
public Map<String, Incident> worstPerCountry(List<Incident> incidents) {
    return incidents.stream()
            .filter(i -> i != null && i.country() != null && i.severity() != null)
            .collect(groupingBy(Incident::country,
                     collectingAndThen(
                         maxBy(Comparator.comparingInt((Incident i) -> i.severity().weight())
                               .thenComparing(Incident::date)),   // tie-break: most recent
                         opt -> opt.orElseThrow())));
}
```
*Talk about:* why `maxBy` returns an `Optional` (a group could theoretically be empty),
why `collectingAndThen` is safe here (groups from `groupingBy` are never empty), and the
tie-break.

---

**Q7 — Percentage breakdown (guard the divide-by-zero).**

```java
public Map<String, Double> percentagePerCountry(List<Incident> incidents) {
    if (incidents == null || incidents.isEmpty()) return Map.of();
    long total = incidents.stream().filter(Objects::nonNull).count();
    if (total == 0) return Map.of();

    return countPerCountry(incidents).entrySet().stream()
            .collect(toMap(Map.Entry::getKey,
                           e -> Math.round(e.getValue() * 10000.0 / total) / 100.0,
                           (a, b) -> a,
                           LinkedHashMap::new));
}
```
*Talk about:* the explicit zero guard, `* 100.0` to force floating-point division, the
rounding-to-2-decimals trick, and that percentages may not sum to exactly 100 after
rounding.

---

**Q8 — Flatten a nested collection.** Most-used tags across all incidents.

```java
public List<String> topTags(List<Incident> incidents, int n) {
    if (incidents == null || n <= 0) return List.of();
    return incidents.stream()
            .filter(Objects::nonNull)
            .flatMap(i -> i.tags() == null ? Stream.<String>empty() : i.tags().stream())
            .filter(t -> t != null && !t.isBlank())
            .map(t -> t.strip().toLowerCase(Locale.ROOT))     // normalise before counting
            .collect(groupingBy(Function.identity(), counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                    .thenComparing(Map.Entry.comparingByKey()))
            .limit(n)
            .map(Map.Entry::getKey)
            .toList();
}
```
*Talk about:* `flatMap` for 1→many, defending against a null inner list, **normalising
before counting** (otherwise `"Kidnap"` and `"kidnap"` are two different tags).

---

**Q9 — Join two datasets (the "lookup map" pattern).** Incident counts by NGO *name*.

```java
public Map<String, Long> countPerNgoName(List<Incident> incidents, List<Ngo> ngos) {
    // Build the lookup ONCE: O(m). Doing ngos.stream().filter(...) inside the loop
    // would make this O(n*m) — this is the key optimisation to say out loud.
    Map<Long, String> nameById = ngos.stream()
            .filter(n -> n != null && n.id() != null)
            .collect(toMap(Ngo::id, Ngo::name, (a, b) -> a));

    return incidents.stream()
            .filter(i -> i != null && i.reportedByNgoId() != null)
            .collect(groupingBy(i -> nameById.getOrDefault(i.reportedByNgoId(), "UNKNOWN"),
                     TreeMap::new, counting()));
}
```
*Talk about:* the O(n+m) vs O(n·m) point; the `toMap` merge function guarding duplicate
IDs; the `"UNKNOWN"` fallback for orphaned references (**ask** whether to skip or bucket).

---

**Q10 — Trend / month-over-month change.**

```java
public Map<YearMonth, Long> monthlyTrend(List<Incident> incidents, YearMonth from, YearMonth to) {
    Map<YearMonth, Long> counts = incidents.stream()
            .filter(i -> i != null && i.date() != null)
            .collect(groupingBy(i -> YearMonth.from(i.date()), counting()));

    Map<YearMonth, Long> filled = new LinkedHashMap<>();
    for (YearMonth m = from; !m.isAfter(to); m = m.plusMonths(1)) {
        filled.put(m, counts.getOrDefault(m, 0L));       // zero-fill the gaps
    }
    return filled;
}
```
*Talk about:* zero-filling; `LinkedHashMap` to keep chronological order; that
percentage-change from a zero baseline is undefined (**ask** what to return).

---

**Q11 — Algorithmic:** merge overlapping travel-restriction ranges (§6.4).
**Q12 — Algorithmic:** longest streak of consecutive incident-free days (§6.5, inverted).
**Q13 — Algorithmic:** find the two incidents closest together in time (sort, then compare
adjacent pairs — O(n log n), and the naive pairwise version is O(n²): say both).

---

## 10. Rapid-fire question bank (with model answers)

Practise saying these out loud in 20–40 seconds each.

### 10.1 Collections

**Q: `ArrayList` vs `LinkedList`?**
> `ArrayList` is a resizable array: O(1) index access, cache-friendly, O(n) inserts in the
> middle. `LinkedList` is a doubly-linked list: O(1) insert/remove at the ends but O(n)
> access and much worse memory locality. In practice I almost always use `ArrayList`, and
> `ArrayDeque` rather than `LinkedList` when I need a queue or stack.

**Q: `HashMap` vs `TreeMap` vs `LinkedHashMap`?**
> `HashMap` is O(1) average with no ordering. `LinkedHashMap` keeps insertion order at a
> small extra cost. `TreeMap` keeps keys sorted, O(log n), and gives you range operations
> like `headMap` and `floorKey`. I pick based on whether I need order, and which order.

**Q: How does `HashMap` work internally?**
> An array of buckets. The key's `hashCode` is spread and mapped to a bucket index.
> Collisions are chained in a linked list, which converts to a balanced tree once a bucket
> exceeds eight entries, keeping worst case at O(log n). When the size exceeds the load
> factor (0.75) times capacity, it resizes and rehashes. It relies entirely on `equals`
> and `hashCode` being correct and stable.

**Q: `HashSet` vs `TreeSet`?**
> `HashSet` is a `HashMap` with a dummy value — O(1), unordered. `TreeSet` is a `TreeMap`
> — O(log n), sorted, and requires elements to be `Comparable` or take a `Comparator`.

**Q: Why must `equals` and `hashCode` be overridden together?**
> Because hash-based collections locate an object by its hash first and only then compare
> with `equals`. If two equal objects have different hash codes they land in different
> buckets and the collection will never find the match — so a lookup silently fails.

**Q: `Comparable` vs `Comparator`?** (see §4.5)

**Q: `fail-fast` vs `fail-safe` iterators?**
> Fail-fast iterators (`ArrayList`, `HashMap`) track a modification count and throw
> `ConcurrentModificationException` if the collection changes underneath them. Fail-safe
> ones (`CopyOnWriteArrayList`, `ConcurrentHashMap`) iterate over a snapshot or a
> weakly-consistent view and never throw, but may not see the newest changes.

**Q: `Collection` vs `Collections`?**
> `Collection` is the root interface; `Collections` is a utility class of static helpers
> like `sort`, `unmodifiableList`, `emptyList`, `reverse`.

### 10.2 Streams

**Q: What is a stream, and how is it different from a collection?**
> A collection stores data; a stream describes a computation over data. Streams are lazy,
> single-use, don't store elements, and don't modify their source.

**Q: Intermediate vs terminal operations?**
> Intermediate operations return a stream and are lazy — nothing happens until a terminal
> operation runs. Terminal operations produce a result or a side effect and consume the
> stream.

**Q: `map` vs `flatMap`?**
> `map` transforms each element one-to-one. `flatMap` transforms each element into a
> stream and concatenates them — you use it when each element contains a collection you
> want to flatten.

**Q: `Collectors.toMap` — what's the danger?**
> The two-argument version throws `IllegalStateException` on a duplicate key, and it
> NPEs on null values. I supply a merge function whenever keys might collide, and a map
> supplier like `LinkedHashMap::new` when order matters.

**Q: `groupingBy` vs `partitioningBy`?**
> `partitioningBy` is a special case that splits on a boolean predicate and **always**
> returns both `true` and `false` keys, even if one side is empty. `groupingBy` builds a
> key per distinct classifier value, and keys with no elements simply don't appear.

**Q: `reduce` vs `collect`?**
> `reduce` folds into a single immutable value with an associative function. `collect` is
> a mutable reduction — it accumulates into a container like a list or map, which is far
> more efficient for building collections.

**Q: When would you NOT use a stream?**
> When a simple loop is clearer; when I need to mutate the source; when I need index-based
> access or early exit with complex control flow; and in extremely hot code where the
> lambda/boxing overhead is measurable. Readability wins by default.

### 10.3 Java language

**Q: Is Java pass-by-value or pass-by-reference?**
> Always pass-by-value. For objects, the *value being copied is the reference*. So a
> method can mutate the object the reference points to, but reassigning the parameter
> inside the method has no effect on the caller's variable.

**Q: `==` vs `.equals()`?**
> `==` compares references for objects and values for primitives. `.equals()` compares
> logical equality as the class defines it.

**Q: What makes a class immutable?**
> Make it `final`, make all fields `private final`, set them only in the constructor,
> provide no setters, and defensively copy any mutable fields both in and out. Immutable
> objects are inherently thread-safe and safe as map keys — `String` and records are the
> examples I reach for.

**Q: Checked vs unchecked exceptions?**
> Checked extend `Exception` and must be declared or handled — meant for recoverable
> conditions. Unchecked extend `RuntimeException` and don't have to be declared — meant
> for programming errors and business-rule violations. Modern frameworks like Spring lean
> almost entirely on unchecked.

**Q: `final`, `finally`, `finalize`?**
> `final` prevents reassignment/overriding/extension. `finally` is the block that always
> runs after try/catch. `finalize` was a deprecated GC hook — never use it; use
> try-with-resources or `Cleaner`.

**Q: Interface vs abstract class?**
> An interface defines a contract; a class can implement many. Since Java 8 interfaces can
> have `default` and `static` methods, but no constructors and no instance state. An
> abstract class can hold state and constructors but only one can be extended. I default
> to interfaces and use an abstract class when subtypes genuinely share implementation
> and state.

**Q: Overloading vs overriding?**
> Overloading is same name, different parameter list, resolved at **compile time**
> (static dispatch). Overriding is a subclass replacing a superclass method with the same
> signature, resolved at **runtime** (dynamic dispatch) — that's polymorphism.

**Q: `String`, `StringBuilder`, `StringBuffer`?**
> `String` is immutable, so concatenating in a loop creates a new object each time.
> `StringBuilder` is a mutable, non-synchronised buffer — the default choice.
> `StringBuffer` is the synchronised, slower legacy version.

**Q: What is the string pool?**
> A JVM-managed area (in the heap since Java 7) where string literals are interned, so
> identical literals share one object to save memory. It's the reason `==` sometimes
> appears to work on strings — which is exactly why you should always use `.equals`.

**Q: Stack vs heap?**
> The stack holds per-thread frames: local variables, primitives, and references — it's
> automatically freed when a method returns. The heap holds all objects and is shared
> between threads and managed by the garbage collector. `Incident i = new Incident(...)`
> puts the reference on the stack and the object on the heap.

**Q: How does garbage collection work?**
> The GC reclaims objects that are no longer reachable from GC roots. Modern collectors
> are generational — most objects die young, so a cheap minor collection sweeps the young
> generation and survivors get promoted to the old generation. I can't force it;
> `System.gc()` is only a suggestion. Memory leaks in Java are usually unintended
> references — a static collection that keeps growing is the classic one.

**Q: JDK vs JRE vs JVM?**
> The JVM executes bytecode. The JRE is the JVM plus the core libraries needed to *run*
> Java. The JDK is the JRE plus the tools needed to *develop* — compiler, debugger.
> `javac` compiles source to platform-independent bytecode; the JVM interprets it and the
> JIT compiles hot paths to native code.

### 10.4 Spring

**Q: Spring vs Spring Boot?**
> Spring is the core framework — DI, AOP, transactions. Spring Boot is a layer on top
> that gives auto-configuration, starter dependencies, sensible defaults, and an embedded
> server, so I get a running application without lots of XML or boilerplate config.

**Q: What is dependency injection and why does it matter?**
> Instead of a class creating its own dependencies, they're supplied from outside. That
> decouples the class from concrete implementations and makes it trivially testable — I
> can pass a mock repository into the constructor with no framework involved at all.

**Q: Why constructor injection?** (see §7.2)

**Q: What does `@Transactional` do, and what's its biggest gotcha?** (see §7.4)

**Q: Where should business logic live and why?** (see §7.1)

---

## 11. Company and role context (short, but say something)

They may ask *"why INSO?"* or *"what interests you about the Portal?"*. Have 30 seconds
ready, and tie it back to engineering.

**Talking points:**
- INSO supports over **1,500 NGOs across 24 of the world's most insecure countries** with
  real-time incident tracking, analysis, mapping and crisis support. **The data is
  genuinely safety-critical** — reliability and correctness aren't abstract quality goals
  here, they affect aid workers' safety.
- The Portal is explicitly described as a **long-term product, not a one-off system**.
  That matches how you like to work: readable code, tests, clear layering, documenting
  decisions, and improving things incrementally rather than rewriting.
- The role spans **backend and frontend** and asks for **ownership through to production**.
  You have both sides — the Spring Boot service work and the React frontend.
- **Security and access control matter**: NGO partners, field teams and HQ staff must see
  different things. Role-based access, careful authorisation, and not leaking data across
  organisations. You can talk about JWT authentication and role-based route/endpoint
  protection from your own project.
- Connect to the exercise domain: aggregating incident data by country, severity and time
  is *literally what the Portal does*. If the exercise uses that domain, say so — *"this
  looks a lot like the incident-reporting side of the Portal"* — it shows engagement.

**Questions to ask them at the end (have 3 ready):**
- "How is the Portal architected today — a modular monolith or services? What's driving
  that choice?"
- "What does ownership look like day to day — is the team on call for what it builds?"
- "How do field-team requirements reach engineering, given how operational the users are?"
- "What does the testing and code-review culture look like on the team?"
- "What's the biggest technical challenge on the Portal over the next year?"

---

## 12. Preparation plan and day-of checklist

### 12.1 Study plan (compress or expand to the time you have)

| Priority | Topic | Where |
|---|---|---|
| **1 (must)** | `Collectors` cookbook — write every snippet from §5.4 from memory | §5 |
| **1 (must)** | Top-N with tie-breaking, from scratch, in under 5 minutes | §5.8 |
| **1 (must)** | Manual aggregation without streams (`merge`, `computeIfAbsent`) | §4.6 |
| **1 (must)** | The edge-case checklist — read it until it's automatic | §8 |
| **2 (high)** | Do practice problems Q1–Q10 in a real IDE, with a timer | §9 |
| **2 (high)** | `Comparator` composition and null-safety | §4.5 |
| **2 (high)** | Service/repository explanation, said out loud | §7.1–7.2 |
| **3 (good)** | Interval merging + consecutive streak algorithms | §6.4–6.5 |
| **3 (good)** | Records, enums, switch expressions — use them naturally | §3 |
| **4 (polish)** | Rapid-fire Q&A, out loud | §10 |
| **4 (polish)** | Company context and your questions for them | §11 |

**Do at least three of the §9 problems by actually typing them into your IDE and running
them.** Reading is not the same as typing under observation.

### 12.2 The night before

- [ ] `java -version` = 17, `mvn -version` OK, IDE opens a Maven project
- [ ] `mvn clean test` run once so the `~/.m2` cache is warm
- [ ] Reread §8 (edge cases) and §5.4 (Collectors)
- [ ] Reread §2.2 (clarifying questions) — pick your favourite six
- [ ] Sleep. Fatigue costs more than one more Collectors method.

### 12.3 During the exercise — the loop

```
1. READ the stub and its test carefully, out loud.
2. RESTATE the requirement in your own words. Confirm.
3. ASK 2–3 clarifying questions (nulls? empties? ties? ordering?).
4. STATE the approach + complexity before coding.
5. CODE the simple, correct version. Loop first if streams feel risky.
6. RUN the test.
7. NAME the edge cases and ask which matter.
8. REFACTOR/optimise only if there's time and they want it.
9. ASK "shall we move on?" — let them steer.
```

### 12.4 Common failure modes to avoid

- **Silence.** The single biggest risk. Narrate everything.
- **Over-engineering step 1.** No interfaces, no builders, no custom exception hierarchy
  on a warm-up. Mention them instead of building them.
- **Fighting a stream you can't remember.** Write the loop, get green, refactor after.
  Explicitly say: *"I'll do this imperatively first for correctness, then tidy it."*
- **Forgetting the tie-break** in a top-N.
- **Collecting a sorted stream into a `HashMap`** and losing the order.
- **Assuming no nulls** without asking.
- **Refusing to use IDE autocomplete or Javadoc.** Using your tools is professional.
- **Arguing** when the interviewer nudges you. Take the hint, thank them, adjust.
- **Not asking any questions at the end.**

---

## 13. One-page cheat sheet (final glance before you walk in)

```java
import static java.util.stream.Collectors.*;

// ---- FILTER ----
.filter(Objects::nonNull)
.filter(i -> country == null || country.equalsIgnoreCase(i.country()))   // optional criterion

// ---- COUNT / SUM / AVG / STATS PER GROUP ----
groupingBy(Incident::country, counting())                        // Map<String, Long>
groupingBy(Incident::country, summingInt(Incident::casualties))  // Map<String, Integer>
groupingBy(Incident::country, averagingInt(Incident::casualties))// Map<String, Double>
groupingBy(Incident::country, summarizingInt(Incident::casualties)) // count/sum/min/max/avg
groupingBy(Incident::country, TreeMap::new, counting())          // SORTED keys
groupingBy(Incident::severity, () -> new EnumMap<>(Severity.class), counting())

// ---- GROUP + TRANSFORM ----
groupingBy(k, mapping(Incident::type, toSet()))
groupingBy(k, filtering(pred, toList()))        // KEEPS empty groups
groupingBy(k, flatMapping(i -> i.tags().stream(), toSet()))
groupingBy(k, collectingAndThen(maxBy(cmp), Optional::orElseThrow))
groupingBy(a, groupingBy(b, counting()))        // two levels
partitioningBy(pred)                            // ALWAYS both true & false keys

// ---- TOP N WITH TIE-BREAK ----
map.entrySet().stream()
   .sorted(Map.Entry.<String,Long>comparingByValue().reversed()
           .thenComparing(Map.Entry.comparingByKey()))
   .limit(n).map(Map.Entry::getKey).toList();

// ---- SORTED MAP OUT (must supply LinkedHashMap!) ----
.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b) -> a, LinkedHashMap::new))

// ---- toMap SAFELY ----
toMap(Incident::country, Incident::casualties, Integer::sum)   // merge fn = no exception

// ---- COMPARATORS ----
Comparator.comparing(Incident::date).reversed()
Comparator.comparingInt(i -> i.severity().weight())
        .thenComparing(Incident::date, Comparator.reverseOrder())
        .thenComparing(Incident::id)                           // deterministic tie-break
Comparator.comparing(Incident::country, Comparator.nullsLast(Comparator.naturalOrder()))
Comparator.comparing(Incident::country, String.CASE_INSENSITIVE_ORDER)

// ---- MANUAL AGGREGATION (no streams) ----
map.merge(key, 1, Integer::sum);
map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
map.getOrDefault(key, 0L);

// ---- MULTI-KEY GROUPING ----
record Key(String country, YearMonth month) {}          // local record — equals/hashCode free

// ---- DEDUPE BY FIELD ----
Set<String> seen = new HashSet<>();
list.stream().filter(i -> seen.add(i.country())).toList();

// ---- DATES ----
YearMonth.from(date)                                    // month bucket, sorts naturally
!d.isBefore(from) && !d.isAfter(to)                     // INCLUSIVE range
ChronoUnit.DAYS.between(a, b) + 1                       // inclusive day count
for (YearMonth m = from; !m.isAfter(to); m = m.plusMonths(1)) { ... }   // zero-fill gaps

// ---- SAFE DEFAULTS ----
if (list == null || list.isEmpty()) return List.of();
stream.mapToInt(...).average().orElse(0.0);             // empty average
(count * 100.0) / total                                 // guard total == 0 first
Objects.requireNonNullElse(x, fallback)
```

**The five sentences that win this interview:**
1. *"Let me restate the requirement to make sure I understood it."*
2. *"Before I code — can the input be null or empty, and how should ties be broken?"*
3. *"My approach is X; that's O(n) time and O(k) space."*
4. *"The edge cases I can see are empty input, nulls, and ties — which of those matter here?"*
5. *"I'll get it correct and readable first, then we can optimise if you'd like."*

---

*Good luck. You know this material — the job on the day is to say it out loud.*


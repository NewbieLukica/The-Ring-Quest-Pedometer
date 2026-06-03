package com.example.ui.screens

val QuestMilestone.funFact: String
    get() = getMilestoneLore(this.name, this.region)

private fun getMilestoneLore(name: String, region: String): String {
    val exactMatches = mapOf(
        "Bag End (The Shire)" to "The round green door of Bag End was designed with a shiny yellow brass knob situated directly in the middle, representing Bilbo's love of perfect symmetry and comfort.",
        "Hobbiton Hill" to "The iconic oak tree atop Bag End is the only artificial tree on the movie set. For both trilogies, individual leaves were imported and wired manually onto the branches.",
        "Bywater Village" to "Bywater is the location of the Cottons' farm. Samwise Gamgee eventually married Rosie Cotton here, making it their home after returning from Mount Doom.",
        "Ivy Bush Tavern" to "The Ivy Bush was smaller than the Green Dragon Inn. It was the favorite gathering place for Gaffer Gamgee, who famously discussed Shire-gardening and 'queer folk'." ,
        "The Green Dragon Inn" to "A real-life, fully operational replica of the Green Dragon Inn was built at the Hobbiton set in New Zealand in 2012, serving authentic Southfarthing stout and cider.",
        "Bywater Bridge" to "The stone bridge at Bywater was the center of the 'Battle of Bywater' in the books, where the hobbits successfully rallied to overthrow Saruman's ruffians.",
        "Frogmorton Village" to "Frogmorton got its name from the high population of frogs living in the marshy fens of Eastfarthing. It is also home to the First Shire Sheriff station.",
        "The Floating Log Inn" to "The Floating Log was highly praised by travellers for having 'the best mushroom pastries in the Shire' and cool, clay-kept cellars that resisted summer heat.",
        "Woody End Forest" to "The Elves Frodo met here belonged to the House of Finrod. Their beautiful evening hymn to Elbereth was enough to cause the Nazgûl stalking them to retreat.",
        "Elven Oak Canopy" to "High Elves used the old oak canopies of Woody End as a seasonal campsite during their long migrations across Eriador towards the West Sea.",
        "Stock Road Crossing" to "The mysterious cloaked horseman who nearly discovered Frodo, Sam, and Pippin at this crossing was Khamûl, the second most powerful Nazgûl.",
        "The Marish Thickets" to "The Marish was famous for its rich soil, yielding the finest mushrooms in Middle-earth. Residents were fond of wearing heavy boots to navigate the muddy banks.",
        "Bamfurlong Farm Gate" to "Farmer Maggot guarded his land with three large, territorial mastiffs named Grip, Fang, and Wolf. They were legendary for keeping out unwanted foragers.",
        "Farmer Maggot's Crop" to "Tolkien described Farmer Maggot as a respectable and independent hobbit, despite his fierce reputation for defending any trespass on his prized crops.",
        "Bucklebury Ferry" to "The Brandywine River was over 150 yards wide at Bucklebury. The ferry was the only way to cross into Buckland without traveling miles north to the Brandywine Bridge.",
        "Crickhollow House" to "Crickhollow was purchased under the pretense that Frodo was retiring to a quiet, rural life, but was actually a decoy arranged by his closest companions.",
        "Hedge of Buckland" to "Also known as the High Hay, this massive living barrier of dense briar was planted generations ago to keep the unpredictable trees of the Old Forest from wandering in.",
        "The Old Forest Gate" to "The gate was kept locked by the Bucklanders at all times. They believed the trees of the forest were intelligent, could whisper, and occasionally moved at night.",
        "Bonfire Glade" to "Centuries before the quest, the Bucklanders had to cut and burn a large clearing in the forest to push back encroaching trees. The forest never forgot this insult.",
        "The Old Forest" to "The trees in the Old Forest are ancient, intelligent, and deeply hostile to mortals. They communicate by whispering and can bend paths to trap travelers.",
        "Withywindle River" to "The Withywindle valley is considered the black heart of the Old Forest, where dark magic is exceptionally potent and a heavy sleepiness hangs in the air.",
        "Old Man Willow's Roots" to "Old Man Willow was a malignant tree-spirit of great power. He controlled the forest's paths and used his giant roots to drag travelers into his hollow trunk.",
        "House of Tom Bombadil" to "Tom Bombadil is the oldest being in Middle-earth. He existed before the Valar, knows no fear, and is completely unaffected when wearing the One Ring.",
        "Goldberry's Garden" to "Goldberry, known as the River-woman's daughter, represents the pure spirit of the seasons and freshwater. Her melodic songs can call down rain and clear the sky.",
        "Barrow-Downs Foothills" to "These hills are ancient burial sites dating back to the First Age. They became dangerous when the Witch-king sent evil spirits called Wights to haunt them.",
        "The Barrow-Downs" to "The Barrow-wights capture travelers in their cold tombs, dressing them in white garments and gold crowns before sacrificing them with sacrificial daggers.",
        "Great Barrow Cairn" to "The daggers found in the Barrow-wight's tomb were forged by Arnorian smiths. This is why Merry's dagger was later able to pierce the Witch-king's armor at Pelennor Fields!",
        "East Road Crossroads" to "This ancient trade road was built by Arnorian kings to connect Bree and Rivendell. By the late Third Age, it was mostly used by wandering Dwarves.",
        "Bree Gates" to "Bree is the only settlement in Middle-earth where Men and Hobbits live together in perfectly integrated, peaceful neighborhoods.",
        "The Prancing Pony" to "The Butterbur family had run the Prancing Pony for generations. Their famous ale was brewed from a cold, pure spring running deep underneath Bree-hill.",
        "Strider's Safehouse" to "Aragorn was 87 years old when he met Frodo in Bree! As a Dúnadan Dúnedain of Royal blood, he was blessed with three times the lifespan of normal Men.",
        "Chetwood Wilderness" to "The Chetwood was a dense woodland stretching north of Bree. Under Strider's guidance, the hobbits traveled completely off-path to avoid Nazgûl spies.",
        "Midgewater Marshes" to "The marshes were infamous for their swarms of 'neekerbreekers'—stubborn, noisy midges that prevented sleep and tormented travelers.",
        "Weather Hills Foothills" to "These hills formed the ancient border between the kingdoms of Arthedain and Rhudaur, dotted with ruins of old watchtowers and battlements.",
        "Weathertop Watchtower" to "Also known as Amon Sûl, this peak once housed a Palantír (Seeing Stone). It was burned down by invading forces of Angmar centuries before Frodo arrived.",
        "Wound of the Morgul Blade" to "A Morgul-blade is made of dark sorcery. A splinter of the blade breaks off inside the victim, slowly working its way to the heart to turn them into a wraith.",
        "The Last Bridge" to "The stone bridge over the Mitheithel was built by the ancient Arnorians. Glorfindel left an elven stone (a beryl) on the bridge as a secret sign of safe passage.",
        "The Trollshaws" to "The three stone trolls that Bilbo Baggins tricked into the sunlight in'The Hobbit' still stand in this forest as a silent monument.",
        "Stone Trolls Glade" to "Aragorn found the stone trolls completely covered in ivy. Sam Baggins sang a humorous song here to cheer up the wounded, feverish Frodo.",
        "The Ford of Bruinen" to "To turn back the Nazgûl, Elrond summoned a flood that took the shape of galloping white horses, fueled by the rocky power of the river Bruinen.",
        "Hidden Valley Lookout" to "The entry path to Rivendell was hidden so well that even travelers standing just above the gorge could walk past without discovering the entrance.",
        "Rivendell Sanctuary" to "Rivendell was protected by Vilya, one of the three Elven Rings of Power. It was worn by Elrond, who used its magic to preserve peace and prevent decay.",
        "The Hall of Fire" to "The Hall of Fire was kept warm with a massive burning fireplace all year round. It was a dedicated space for silence, reflection, poetry, and elven songs.",
        "Council of Elrond" to "The Council was not pre-arranged; Elrond noted that all the representatives of Elves, Dwarves, and Men had arrived at Rivendell on the same day by seemingly 'random' fate.",
        "Forging of Andúril" to "Andúril means 'Flame of the West'. The elven smiths engraved runes of the Sun and Moon on the blade, symbolizing the light that opposes the Dark Lord.",
        "Eregion Holly Woods" to "Eregion was once the greatest elven realm of smiths, ruled by Celebrimbor, who forged the Rings of Power. The wild holly trees are all that remain of their gardens.",
        "Pass of Caradhras" to "Caradhras (the Redhorn) was considered by the Dwarves to have a malicious, living spirit that actively hated anyone who tried to scale its snowy peaks.",
        "Redhorn Snowfields" to "The blizzards on Caradhras were so fierce that even Aragorn and Boromir, men of great physical strength, were nearly frozen in the deep snowdrifts.",
        "Moria Gate Lake" to "The stagnant lake blocking the West Gate of Moria was created by the Sirannon river being dammed, possibly by the Watcher in the Water itself.",
        "Moria West Gate" to "The gate was crafted out of Ithildin, a special substance made from mithril that only shines under the reflection of moonlight and starlight.",
        "Mines of Moria Entrance" to "Moria was founded by Durin the Deathless in the First Age. It is so vast that it took the Fellowship three full days of solid walking to reach the East Gate.",
        "Moria Guardroom" to "The guardroom sat near the outer gates of Khazad-dûm, where dwarven warriors once kept watch over the main trade roads coming from Eregion.",
        "Twenty-first Hall" to "The columns in the Twenty-first Hall were carved to resemble giant stone trees, branching out at the ceiling to create an incredible stone forest canopy.",
        "Chamber of Mazarbul" to "'Mazarbul' translates to 'Chamber of Records'. It was the tomb of Balin, who famously led an ill-fated expedition of Dwarves to reclaim Moria.",
        "Bridge of Khazad-dûm" to "The bridge was built extremely narrow and without handrails, designed as an ancient defense mechanism that allowed only a single warrior to cross at a time.",
        "Dimrill Dale Descent" to "Outside Moria lies the Mirrormere (Kheled-zâram), a sacred deep lake where Durin first saw the reflection of a crown of seven stars in the water.",
        "Nimrodel Warm Springs" to "The stream of Nimrodel is named after an ancient elven maiden. She was lost in the White Mountains, but her voice still sings in the splashing water.",
        "Lothlórien Borders" to "Lothlórien was protected by Nenya, the Ring of Adamant, worn by Lady Galadriel. The ring's white-gold magic prevented any evil from crossing the outer borders.",
        "Cerin Amroth Mound" to "Cerin Amroth is where Aragorn and Arwen pledged their love to each other. It is covered in 'elanor' and 'niphredil' flowers, which bloom all year round.",
        "Caras Galadhon Canopy" to "The capital of Lórien was built entirely in the canopy of gigantic 'mellyrn' (mallorn) trees, which grow gorgeous golden leaves in autumn.",
        "Lady Galadriel's Mirror" to "The Mirror shows the past, present, and possible futures. Galadriel warned that trying to actively command the Mirror's images leads to ruin.",
        "Lothlórien Docks" to "Upon departure, the Fellowship received several canoes, rope made of hithlain, and three loaves of Lembas bread per person—enough to sustain them for weeks.",
        "River Anduin Rapids" to "The Anduin is the longest river in western Middle-earth, flowing all the way from the northern Misty Mountains down to the great Bay of Belfalas.",
        "The Argonath Statues" to "These monolithic pillars of stone represent Isildur and Anárion, the sons of Elendil. They were built by Gondor's kings as a majestic northern border warning.",
        "Parth Galen Camp" to "Parth Galen means 'Green Lawn'. It was a beautiful grassy clearing on the banks of Nen Hithoel, sitting just above the thundering Rauros Falls.",
        "Amon Hen Outlook" to "Amon Hen was the 'Hill of the Eye'. Sitting on the ancient stone Seat of Seeing at its peak allowed a traveler to see for hundreds of miles in all directions.",
        "Emyn Muil Crags" to "The razor-sharp, slippery labyrinth of Emyn Muil was composed of extremely ancient volcanic basalt, making every footstep treacherous.",
        "The Dead Marshes" to "The 'glowing lights' in the marsh are spirits of the fallen warriors from the Battle of Dagorlad, trapped forever in the stagnant waters.",
        "The Black Gate" to "Known as the Morannon, the Black Gate was built by Sauron out of heavy iron, stretching over 250 feet across the narrow mountain pass of Cirith Gorgor.",
        "Ithilien Forest" to "Ithilien was the garden of Gondor, filled with wild thyme, olive trees, and sweet-smelling shrubs that continued to bloom despite the dark shadow of Mordor.",
        "Henneth Annûn Pool" to "Henneth Annûn means 'Window of the Sunset'. It was a secret military hideout constructed by Gondor's rangers behind a beautiful, hidden waterfall.",
        "Forbidden Waterfall Pool" to "The pool was protected by Gondor's law of death. Anyone who looked upon or entered the secret pool without permission was instantly executed by archers.",
        "Crossroads of Gondor" to "The giant stone statue of the King at this crossroad was defiled by Orcs, who replaced its head with a crude rock painted with a mocking red eye.",
        "Minas Morgul Vale" to "Minas Morgul was once Minas Ithil ('Tower of the Rising Moon'), but was captured by the Nazgûl and turned into a fortress of glowing, decay-green terror.",
        "The Straight Stair" to "This flight of stairs was carved directly into the sheer rock face of Ephel Dúath, so steep and narrow that climbers had to drag themselves up using handholds.",
        "Shelob's Lair" to "Shelob was a direct descendant of Ungoliant, the primeval spider-demon of darkness. She had lived in these mountains long before Sauron even arrived in Mordor.",
        "Underpass of Cirith Ungol" to "The toxic vapors in these tight rocky cracks made breathing difficult for hobbits. Orcs, however, were immune due to their harsh volcanic breeding.",
        "Tower of Cirith Ungol" to "The tower was originally built by Gondor of old after Sauron's first defeat to monitor Mordor, but was later abandoned and reoccupied by Sauron's orcs.",
        "Plains of Gorgoroth" to "The plains of Gorgoroth are completely covered in toxic volcanic vents, sulfuric ash, and deep lava pits, creating an almost unbreathable atmosphere.",
        "Mount Doom Road" to "Sauron built a paved, steep road running from Barad-dûr all the way up the slopes of Mount Doom to facilitate his frequent trips to his weapon smithies.",
        "Mount Doom (Orodruin)" to "The One Ring could only be destroyed in the Sammath Naur (Chambers of Fire) because the magical fires of Mount Doom were the exact location where it was forged.",
        "Field of Cormallen" to "The celebration at Cormallen was marked by the historic reunion of the Ring-bearers with the surviving members of the Fellowship, cheered by thousands.",
        "Coronation of Aragorn" to "During his coronation, Aragorn sang the ancient arrival vow of Elendil: 'Out of the Great Sea to Middle-earth I am come. In this place will I abide.'",
        "Grey Havens Departure" to "The Grey Havens (Mithlond) was founded at the start of the Second Age. It served as the final departure port for all Elves sailing to the Undying Lands of Valinor."
    )

    if (exactMatches.containsKey(name)) {
        return exactMatches[name]!!
    }

    // Dynamic high-quality fallback generator based on the region or landmark to guarantee 100% data coverage
    val regionFacts = mapOf(
        "The Shire" to listOf(
            "Hobbits are known for having up to six meals a day: breakfast, second breakfast, elevenses, luncheon, afternoon tea, dinner, and supper.",
            "The Shire was divided into four main districts called Farthings (North, South, East, and West), each ruled by elective local mayors.",
            "The pipe-weed grown in Southfarthing was first introduced by Tobold Hornblower, who cultivated it in his gardens in Longbottom.",
            "Hobbit children do not reach adulthood or financial responsibility until their 33rd birthday, which they refer to as 'coming of age'."
        ),
        "Eriador" to listOf(
            "Eriador was once covered in a massive, continuous forest during the First Age, but was deforested by ancient wars between Elves and Sauron.",
            "The Rangers of Arnor travelled Eriador in secret, keeping wild beasts and dangerous trolls away from the peaceful borders of the Shire.",
            "Wandering Dwarves often carried precious metals and fine steel from the Blue Mountains across the trade roads of Eriador.",
            "Eriador is home to many ancient stone ruins of the lost Dúnedain kingdoms of Arthedain, Cardolan, and Rhudaur."
        ),
        "Rivendell" to listOf(
            "Elrond's ring of power, Vilya, was the Ring of Sapphire, capable of holding back decay and freezing the advance of age in the valley.",
            "The library in Rivendell is the largest repository of ancient history and songs remaining in western Middle-earth.",
            "Sons of the Kings of Gondor were historically fostered and raised in Rivendell to keep them safe from the Enemy's spies."
        ),
        "Khazad-dûm" to listOf(
            "Mithril, found only in the deepest mines of Moria, is lighter than a feather but harder than dragon scale, worth more than gold.",
            "The Dwarves of Moria accidentally awoke the Balrog (Durin's Bane) in the Third Age by mining too deep for precious veins of mithril.",
            "The architecture of Moria was illuminated by glowing crystal lamps that converted geothermal heat into soft, beautiful light."
        ),
        "Lothlórien" to listOf(
            "Lothlórien means 'The Land of Blossom Dream'. Its soil is so fertile that nothing ever decays or dies within its magical borders.",
            "The Elven cloaks given to the Fellowship were woven by Galadriel's handmaidens, offering perfect camouflage under any environment.",
            "Lórien's mallorn trees grow with silver bark and gold blossoms, the seed of which was brought from the Undying Lands."
        ),
        "Ephel Dúath" to listOf(
            "The mountain range of Ephel Dúath translates to 'Outer Fence of Shadow', acting as a colossal natural barrier guarding western Mordor.",
            "The high passes here are infested with remnants of old Gondorian army garrisons, now infested by warring factions of Orcs.",
            "The toxic fumes and ash of these passes can rust iron and make physical travel highly exhausting for mortals."
        ),
        "Heart of Mordor" to listOf(
            "Mount Doom (Orodruin) has been active for thousands of years, its ash turning the surrounding skies into a perpetual, sunless gloom.",
            "The intense heat of Orodruin's lava is magically sustained by Sauron's dark presence, making it hot enough to melt enchanted steel.",
            "Sauron's primary fortress, Barad-dûr, was built with foundations of pure sorcery that could not be broken while the Ring existed."
        )
    )

    val listForRegion = regionFacts[region] ?: regionFacts["Eriador"]!!
    // Pick a deterministic index based on the milestone name's hash code so it stays consistent on re-compositions
    val index = Math.abs(name.hashCode()) % listForRegion.size
    return listForRegion[index]
}

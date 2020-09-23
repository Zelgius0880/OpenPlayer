package com.zelgius.openplayer.ui.preview

import com.zelgius.openplayer.model.Album
import com.zelgius.openplayer.model.Track
import com.zelgius.openplayer.parseAsJsonArray

val SAMPLE_ALBUM_LIST = """
    [
   {
      "name":"1",
      "type":"album",
      "uri":"local:album:md5:7e8abd7abef27aeb1a9e2d49e1f767de",
      "__model__":"Ref"
   },
   {
      "name":"1",
      "type":"album",
      "uri":"local:album:md5:bc11767bbc9863199ea68df7bcde8444",
      "__model__":"Ref"
   },
   {
      "name":"1",
      "type":"album",
      "uri":"local:album:md5:986113b19908b4aa284876403c17866e",
      "__model__":"Ref"
   },
   {
      "name":"1",
      "type":"album",
      "uri":"local:album:md5:6b515c3bdf0185831c5612207b77e8fd",
      "__model__":"Ref"
   },
   {
      "name":"1",
      "type":"album",
      "uri":"local:album:md5:b4686a5d5caee6fe7a4cca031d7ee5fe",
      "__model__":"Ref"
   },
   {
      "name":"1",
      "type":"album",
      "uri":"local:album:md5:7a4343afed779ec2430f15124f185ecd",
      "__model__":"Ref"
   },
   {
      "name":"1",
      "type":"album",
      "uri":"local:album:md5:edd735f2d1e934ef2206a1db68d1d9a0",
      "__model__":"Ref"
   },
   {
      "name":"1",
      "type":"album",
      "uri":"local:album:md5:904e481ff8f8900433d038a52d9336e2",
      "__model__":"Ref"
   },
   {
      "name":"21st Century Breakdown",
      "type":"album",
      "uri":"local:album:md5:0cbb3a4227f6f3830f099d3a7e8eb275",
      "__model__":"Ref"
   },
   {
      "name":"A poil dans la forêt",
      "type":"album",
      "uri":"local:album:md5:75594571f37d5ec5b2ceea3bec27c009",
      "__model__":"Ref"
   },
   {
      "name":"A Thousand Suns",
      "type":"album",
      "uri":"local:album:md5:309fe0765b765fb3d802629c3f31b2b2",
      "__model__":"Ref"
   },
   {
      "name":"ABBA Gold (40th Anniversary Limited Edition)",
      "type":"album",
      "uri":"local:album:md5:b38bfe5829a31f781fa574bb2b88f6ff",
      "__model__":"Ref"
   },
   {
      "name":"American Idiot",
      "type":"album",
      "uri":"local:album:md5:aae5bbe34cd17432d2acd2de9ac8d5ea",
      "__model__":"Ref"
   },
   {
      "name":"Bastion Original Soundtrack",
      "type":"album",
      "uri":"local:album:md5:3f5eaf6214611ae5346a902da32af9fd",
      "__model__":"Ref"
   },
   {
      "name":"Bravely Default Flying Fairy Original Soundtrack [Disc 1]",
      "type":"album",
      "uri":"local:album:md5:7922ef49045205b9c2340857bd2bb9b2",
      "__model__":"Ref"
   },
   {
      "name":"Bravely Default Flying Fairy Original Soundtrack [Disc 2]",
      "type":"album",
      "uri":"local:album:md5:a98addee6656fa019ae0f55c923bc8a8",
      "__model__":"Ref"
   },
   {
      "name":"BRAVELY SECOND END LAYER Original Soundtrack",
      "type":"album",
      "uri":"local:album:md5:bea9052effbfd70f936524c236351ac6",
      "__model__":"Ref"
   },
   {
      "name":"Cadence of Hyrule Gamerip Soundtrack",
      "type":"album",
      "uri":"local:album:md5:dc1de90738d859590bd5dc8732b68618",
      "__model__":"Ref"
   },
   {
      "name":"Command & Conquer: Red Alert 3 - Premier Edition Soundtrack",
      "type":"album",
      "uri":"local:album:md5:a8e51322b639c2d27b994a79fdb887d5",
      "__model__":"Ref"
   },
   {
      "name":"Direct Rip",
      "type":"album",
      "uri":"local:album:md5:b846b19dc5f6915ab5e1868800742d24",
      "__model__":"Ref"
   },
   {
      "name":"Donjon de Naheulbeuk",
      "type":"album",
      "uri":"local:album:md5:89218600962f26ae631fb0eb5b431b3b",
      "__model__":"Ref"
   },
   {
      "name":"Donjon de Naheulbeuk",
      "type":"album",
      "uri":"local:album:md5:29c6aa5da679c81f07b3fb066d70bdbb",
      "__model__":"Ref"
   },
   {
      "name":"Donjon de Naheulbeuk",
      "type":"album",
      "uri":"local:album:md5:f4ca35b6e26e8a2237d38ecd3ee508c2",
      "__model__":"Ref"
   },
   {
      "name":"Donjon de Naheulbeuk",
      "type":"album",
      "uri":"local:album:md5:72bc014c27fc86dc5aaca7df333a89b8",
      "__model__":"Ref"
   },
   {
      "name":"donjon de naheulbeuk",
      "type":"album",
      "uri":"local:album:md5:d9e33d06b2b3ba2bfe208aa669dd186c",
      "__model__":"Ref"
   },
   {
      "name":"Father of All...",
      "type":"album",
      "uri":"local:album:md5:7787e18a29b86de1a39be2b51b8f1228",
      "__model__":"Ref"
   },
   {
      "name":"Final Fantasy IV Original Soundtrack Disk 1",
      "type":"album",
      "uri":"local:album:md5:534f8652a8542904bc72e050e5bd3173",
      "__model__":"Ref"
   },
   {
      "name":"Fire Emblem Fates OST [Disc 1]",
      "type":"album",
      "uri":"local:album:md5:28e28854855efdfc6f1689d804790753",
      "__model__":"Ref"
   },
   {
      "name":"Fire Emblem Fates OST [Disc 2]",
      "type":"album",
      "uri":"local:album:md5:414978e1d9053a129c6366c63bccd4b1",
      "__model__":"Ref"
   },
   {
      "name":"Fire Emblem: Awakening",
      "type":"album",
      "uri":"local:album:md5:f38cc3f98b2d78a3a7cc1a2cf167c315",
      "__model__":"Ref"
   },
   {
      "name":"Fire Emblem: Three Houses Sound Selection",
      "type":"album",
      "uri":"local:album:md5:03d541d5a4c78a8339184a2b6a3526c1",
      "__model__":"Ref"
   },
   {
      "name":"Game of Thrones",
      "type":"album",
      "uri":"local:album:md5:855d12e26409db4546e9b236777cdcc1",
      "__model__":"Ref"
   },
   {
      "name":"Genso Suikoden Tierkreis Original Soundtrack",
      "type":"album",
      "uri":"local:album:md5:6cef7496b5858321f51fabb900a602e4",
      "__model__":"Ref"
   },
   {
      "name":"Giédré, mon premier album genre pannini",
      "type":"album",
      "uri":"local:album:md5:5713fecf0e226abac6c98fa99ba3a844",
      "__model__":"Ref"
   },
   {
      "name":"Gundam W OPERATION 1",
      "type":"album",
      "uri":"local:album:md5:ddb6d7f8653544de3a3bc8600fa68cef",
      "__model__":"Ref"
   },
   {
      "name":"Gyakuten Saiban 05 CD 2",
      "type":"album",
      "uri":"local:album:md5:475a3952467cf2edd46a8f1046df812b",
      "__model__":"Ref"
   },
   {
      "name":"Legend of Zelda : Majora's Mask ",
      "type":"album",
      "uri":"local:album:md5:8b63746c0e40f76e78dd9f776ba40df7",
      "__model__":"Ref"
   },
   {
      "name":"Live From Mexico (Deluxe Edition)",
      "type":"album",
      "uri":"local:album:md5:5c417c74d511a916c5c2a2ed0be90ba5",
      "__model__":"Ref"
   },
   {
      "name":"Living Things",
      "type":"album",
      "uri":"local:album:md5:80ed7028c4cb2751b96050e1480e8a95",
      "__model__":"Ref"
   },
   {
      "name":"Living Things",
      "type":"album",
      "uri":"local:album:md5:eedc6e1effa3965cd04df71e67d515fb",
      "__model__":"Ref"
   },
   {
      "name":"Machins de Taverne",
      "type":"album",
      "uri":"local:album:md5:74fa5d6c89f72a1c31605c60961fed2d",
      "__model__":"Ref"
   },
   {
      "name":"Mario & Luigi RPG 3!!!",
      "type":"album",
      "uri":"local:album:md5:5b6a9a755e685df8453f32e46a57b38b",
      "__model__":"Ref"
   },
   {
      "name":"Mario & Luigi RPG 3!!!",
      "type":"album",
      "uri":"local:album:md5:ba991861c637e5d62c172e50687a6df8",
      "__model__":"Ref"
   },
   {
      "name":"Meteora",
      "type":"album",
      "uri":"local:album:md5:68c2d04abcded9470f5281547694968b",
      "__model__":"Ref"
   },
   {
      "name":"Minutes To Midnight",
      "type":"album",
      "uri":"local:album:md5:5e8a1405c4a798c47341a88d49e4178b",
      "__model__":"Ref"
   }
]
""".parseAsJsonArray<Album>()?: listOf()

val SAMPLE_TRACK_LIST = """
    [
   {
      "name":"In My Remains",
      "type":"track",
      "uri":"local:track:Linkin%20Park%20-%20In%20My%20Remains.mp3",
      "__model__":"Ref"
   },
   {
      "name":"Castle Of Glass",
      "type":"track",
      "uri":"local:track:Linkin%20Park%20-%20Castle%20Of%20Glass.mp3",
      "__model__":"Ref"
   },
   {
      "name":"Victimized",
      "type":"track",
      "uri":"local:track:Linkin%20Park%20-%20Victimized.mp3",
      "__model__":"Ref"
   },
   {
      "name":"Roads Untraveled",
      "type":"track",
      "uri":"local:track:Linkin%20Park%20-%20Roads%20Untraveled.mp3",
      "__model__":"Ref"
   },
   {
      "name":"Skin To Bone",
      "type":"track",
      "uri":"local:track:Linkin%20Park%20-%20Skin%20To%20Bone.mp3",
      "__model__":"Ref"
   },
   {
      "name":"Until It Breaks",
      "type":"track",
      "uri":"local:track:Linkin%20Park%20-%20Until%20It%20Breaks.mp3",
      "__model__":"Ref"
   },
   {
      "name":"Tinfoil",
      "type":"track",
      "uri":"local:track:Linkin%20Park%20-%20Tinfoil.mp3",
      "__model__":"Ref"
   }
]
""".parseAsJsonArray<Track>()?: listOf()
AstralSorcery.research(event => {
  event.create('kubejs:starry_diamond')
    .title('Starry Diamond')
    .tier('shimmer')
    .position(0, 3)
    .icon('minecraft:diamond')
    .lookup('minecraft:diamond')
    .connect('astralsorcery:welcome')
    .text('A diamond shaped by the stars.\\nIt is crafted on the altar.')
    .recipe('kubejs:test/altar_basic')
    .constellation('aevitas')
    .structure('astralsorcery:altar_t2', 0)

  event.modify('astralsorcery:welcome', node => {
    node.text('<color="#55FFFF">This page was added by KubeJS.</color>')
  })

  event.create('kubejs:stellar_lore')
    .title('Stellar Altar')
    .tier('radiance')
    .position(2, 2)
    .icon('kubejs:stellar_altar')
    .altarRecipe('kubejs:test/altar_stellar')
    .text('The fifth altar, added by KubeJS.')
    .constellation('kubejs:lyra')
    .structure('kubejs:stellar_altar')

  event.remove('astralsorcery:chisel')
})

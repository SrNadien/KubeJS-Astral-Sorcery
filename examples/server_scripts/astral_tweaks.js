AstralSorcery.lumenBindings(event => {
  event.modify('aevitas', binding => {
    binding.potionDuration(100, 200)
    binding.slot('helmet', slot => slot.cost(5).chance(0.75))
    binding.scaleModifiers(2)
  })

  event.create('kubejs:test_binding')
    .potion('minecraft:haste', 400, 800, 1, 1)
    .slot('tool', slot => slot.usageType('block_break').cost(2).chance(0.5).modifier('astralsorcery:block_break_speed', 1, 0.25))

  event.map('armara', 'kubejs:test_binding')
  event.remove('discidia')
})

AstralSorcery.config(event => {
  event.set('general.dayLength', 30000)
  event.set('perks.perkLevelCap', 50)
  event.set('tiles.tree_beacon.range', 20)
})

AstralSorcery.focalPoint(event => {
  event.flightRadius(12)
  event.flightDuration(200)
  event.starlightMultiplier(2)
  event.constellationMultiplier('vicio', 3)
  event.linkDistance(24)
  event.transmutationSpeed(2)
})

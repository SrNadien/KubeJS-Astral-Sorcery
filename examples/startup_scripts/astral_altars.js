AstralSorcery.altarTiers(event => {
  event.create('stellar')
    .base('radiance')
    .researchTier('radiance')
    .block('kubejs:stellar_altar')
})

StartupEvents.registry('block', event => {
  event.create('kubejs:celestial_altar', 'astralsorcery:altar')
    .displayName('Celestial Altar')
    .baseTier('luminance')
    .exclusive()
    .structureBlock(0, -1, 0, 'minecraft:gold_block')

  event.create('kubejs:stellar_altar', 'astralsorcery:altar')
    .displayName('Stellar Altar')
    .baseTier('radiance')
    .realTier('stellar')
    .structureLayer(-1, [
      'D   D',
      '     ',
      '  D  ',
      '     ',
      'D   D'
    ], { D: 'minecraft:diamond_block' })
})

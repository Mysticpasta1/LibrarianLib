package com.teamwizardry.librarianlib.features.particlesystem

import com.teamwizardry.librarianlib.features.particlesystem.bindings.StoredBinding
import org.magicwerk.brownies.collections.GapList
import java.util.*

class ParticleSystem {
    val particles = GapList<DoubleArray>()
    val updateModules: MutableList<ParticleUpdateModule> = mutableListOf()
    val postUpdateModules: MutableList<ParticleBatchUpdateModule> = mutableListOf()
    val renderPrepModules: MutableList<ParticleUpdateModule> = mutableListOf()
    val renderModules: MutableList<ParticleRenderModule> = mutableListOf()
    val lifetime = StoredBinding(0, 1)
    val age = StoredBinding(1, 1)
    var poolSize = 1000
    val particlePool = ArrayDeque<DoubleArray>(poolSize)
    var fieldCount = 2
        private set

    fun bind(size: Int): StoredBinding {
        val binding = StoredBinding(fieldCount, size)
        fieldCount += size
        return binding
    }

    fun update() {
        val iter = particles.iterator()
        for(particle in iter) {
            val lifetime = this.lifetime.get(particle, 0) - 1
            if(lifetime <= 0) {
                iter.remove()
                if(particlePool.size < poolSize)
                    particlePool.push(particle)
                continue
            }
            this.lifetime.set(particle, 0, lifetime)
            update(particle)
        }

        for(i in 0 until postUpdateModules.size) {
            postUpdateModules[i].update(particles)
        }
    }

    private fun update(particle: DoubleArray) {
        for(i in 0 until updateModules.size) {
            updateModules[i].update(particle)
        }
    }

    fun render() {
        for(i in 0 until renderModules.size) {
            renderModules[i].render(particles, renderPrepModules)
        }
    }

    fun addParticle(lifetime: Double, vararg params: Double): DoubleArray {
        val particle = DoubleArray(fieldCount)
        particle[0] = lifetime
        particle[1] = 0.0
        (2 until particle.size).forEach { i ->
            if(i-2 < params.size)
                particle[i] = params[i-2]
            else
                particle[i] = 0.0
        }
        particles.add(particle)
        return particle
    }
}
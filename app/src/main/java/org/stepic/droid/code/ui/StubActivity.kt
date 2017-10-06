package org.stepic.droid.code.ui

import android.graphics.Typeface
import android.os.Bundle
import kotlinx.android.synthetic.main.code_editor.*
import org.stepic.droid.R
import org.stepic.droid.base.FragmentActivityBase

class StubActivity : FragmentActivityBase() {


    private val code = """
from mpl_toolkits.mplot3d import Axes3D
import numpy as np
import pylab as py
import explicit_euler_method
import implicit_euler_method
import adams_predictor_corrector_method
import runge_kutta_method

delta = 10.
b = 8. / 3
dt = 0.01
t_max = 100.

fig = py.figure()
ax = fig.add_subplot(111, projection='3d')

f, ppt = py.subplots()


def solve_with_method(method, system, conditions, ax, ppt, labes_suffix):
    r = system["r"]
    xs, ys, zs, ts = method.solve(system, conditions)

    ax.plot(xs, ys, zs, label=("r = %f " + labes_suffix) % r)

    ppt.plot(ts, xs, label=("x(t), r=%f " + labes_suffix) % r)
    ppt.plot(ts, ys, label=("y(t), r=%f " + labes_suffix) % r)
    ppt.plot(ts, zs, label=("z(t), r=%f " + labes_suffix) % r)


for r in np.arange(4., 4.5, 35.):
    print "r = %f" % r
    system = {
        "x": lambda x, y, z: -delta * x + delta * y,
        "y": lambda x, y, z: -x * z + r * x - y,
        "z": lambda x, y, z: x * y - b * z,
        "r": r,
        "delta": delta,
        "b": b,
        "dt": dt,
        "t_max": t_max
    }

    # начальные условия (x, y, z)
    conditions = (1., 2., 3.)
    solve_with_method(explicit_euler_method, system, conditions, ax, ppt, "ex Euler")
    solve_with_method(implicit_euler_method, system, conditions, ax, ppt, "im Euler")
    solve_with_method(runge_kutta_method, system, conditions, ax, ppt, "Runge-Kutta")
    solve_with_method(adams_predictor_corrector_method, system, conditions, ax, ppt, "Adams P-C")

ax.legend()
ppt.legend()
py.show()
        """.trimMargin()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.code_editor)

        codeEdit.setText(code)
        codeEdit.typeface = Typeface.MONOSPACE
    }
}
package com.zg.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zg.mvc.annontation.Autowired;
import com.zg.mvc.annontation.Cmds;
import com.zg.mvc.annontation.Controller;
import com.zg.mvc.annontation.RequestParam;
import com.zg.mvc.annontation.Service;

public class DispactherServlet extends HttpServlet
{
    // 用来放class类
    List<String> classNames = new ArrayList<String>();
    // ioc容器
    Map<String, Object> iocMap = new HashMap<String, Object>();
    
    Map<String, Object> handle = new HashMap<String, Object>();
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        // 扫描基础包用来扫描注解
        basePackageScan("com.zg.mvc");
        // 将bean扫描到容器中
        doInstance();
        // autowire初始化注入
        doAutowire();
        // 初始化路径对应的方法C核心
        initHandle();
    }
    
    private void doAutowire()
    {
        for (Entry<String, Object> entry : iocMap.entrySet())
        {
            try
            {
                Object instance = entry.getValue();
                Class<?> clazz = instance.getClass();
                Annotation[] annotations = clazz.getAnnotations();
                for (Annotation annotation : annotations)
                {
                    if (Controller.class.isAssignableFrom(annotation.getClass()))
                    {
                        Field[] fields = clazz.getDeclaredFields();
                        for (Field field : fields)
                        {
                            Annotation[] anns = field.getAnnotations();
                            for (Annotation ann : anns)
                            {
                                if (Autowired.class.isAssignableFrom(ann.getClass()))
                                {
                                    Autowired annotation2 = field.getAnnotation(Autowired.class);
                                    String value = annotation2.value();
                                    if (value != null && value.length() > 0)
                                    {
                                        Object object = iocMap.get(value);
                                        field.setAccessible(true);
                                        field.set(instance, object);
                                    }
                                    else
                                    {
                                        String name = field.getName();
                                        Object object = iocMap.get(name);
                                        field.setAccessible(true);
                                        field.set(instance, object);
                                    }
                                }
                                else
                                {
                                    continue;
                                }
                            }
                        }
                    }
                    else
                    {
                        continue;
                    }
                }
            }
            catch (IllegalAccessException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private void basePackageScan(String basePackage)
    {
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
        String path = url.getFile();
        File file = new File(path);
        String[] list = file.list();
        for (String path1 : list)
        {
            File file1 = new File(path + path1);
            if (file1.isDirectory())
            {
                basePackageScan(basePackage+"."+path1);
            }
            else
            {
                classNames.add(basePackage+"."+file1.getName());
            }
        }
    }
    
    private void doInstance()
    {
        for (String className : classNames)
        {
            try
            {
                className = className.replace(".class", "");
                Class<?> clazz = Class.forName(className);
                // 获取类上的注解
                Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
                for (Annotation annotation : declaredAnnotations)
                {
                    // 将带有@Controller的加入ioc容器以注解中的value值为键，如果值为空那么以类名为键
                    if (Controller.class.isAssignableFrom(annotation.getClass()))
                    {
                        Object instance = clazz.newInstance();
                        Cmds controller = instance.getClass().getAnnotation(Cmds.class);
                        String value = controller.value();
                        if (value != null && value.length() > 0)
                        {
                            iocMap.put(value, instance);
                        }
                        else
                        {
                            String name = clazz.getName();
                            iocMap.put(name, instance);
                        }
                    }
                    else if (Service.class.isAssignableFrom(annotation.getClass()))
                    {
                        Object instance = clazz.newInstance();
                        Service service = instance.getClass().getAnnotation(Service.class);
                        String value = service.value();
                        if (value != null && value.length() > 0)
                        {
                            iocMap.put(value, instance);
                        }
                        else
                        {
                            String name = clazz.getName();
                            iocMap.put(name, instance);
                        }
                    }
                    else
                    {
                        continue;
                    }
                }
                
            }
            catch (ClassNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InstantiationException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private void initHandle()
    {
        for (Entry<String, Object> entry : iocMap.entrySet())
        {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            Annotation[] annotations = clazz.getAnnotations();
            for (Annotation annotation : annotations)
            {
                if (Controller.class.isAssignableFrom(annotation.getClass()))
                {
                    Cmds cmds = instance.getClass().getAnnotation(Cmds.class);
                    String classValue = cmds.value();// 类上的CMds中的值
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods)
                    {
                        Annotation[] anns = method.getAnnotations();
                        for (Annotation ann : anns)
                        {
                            if (Cmds.class.isAssignableFrom(ann.getClass()))
                            {
                                Cmds methodCmds = method.getAnnotation(Cmds.class);
                                String methodCmdsValue = methodCmds.value();
                                String handleKey = classValue +methodCmdsValue;
                                handle.put(handleKey, method);
                            }
                            else
                            {
                                continue;
                            }
                        }
                    }
                }
                else
                {
                    continue;
                }
            }
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();// 获取/项目名称/....
        String cmds = requestURI.replace(contextPath, "");// 剩下的就是命令
        Method method = (Method)handle.get(cmds);
        String classCmds ="/" + cmds.split("/")[1];
        Object object = iocMap.get(classCmds);
        Object[] args = getArgs(req, resp, method);
        try
        {
            
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/html;charset=UTF-8");
            Object invoke = method.invoke(object, args);
            PrintWriter printWriter = resp.getWriter();
            printWriter.write(invoke.toString());
            printWriter.close();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        super.doGet(req, resp);
    }
    
    private Object[] getArgs(HttpServletRequest request, HttpServletResponse response, Method method)
    {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        int args_i = 0;
        int index = 0;
        Parameter[] parameters = method.getParameters();
        for (Class<?> parameter : parameterTypes)
        {
            if (parameter.getClass().isAssignableFrom(ServletRequest.class))
            {
                args[args_i++] = request;
            }
            if (parameter.getClass().isAssignableFrom(ServletResponse.class))
            {
                args[args_i++] = response;
            }
            // 按method中param的顺序去整参数
            Annotation[] parameterAnnotations = method.getParameterAnnotations()[index];
            for (Annotation a : parameterAnnotations)
            {
                if (RequestParam.class.isAssignableFrom(a.getClass()))
                {
                    RequestParam param = parameters[index].getAnnotation(RequestParam.class);
                    String value = param.value();// 值
                    args[args_i++] = request.getParameter(value);
                }
                else
                {
                    continue;
                }
            }
            index++;
        }
        
        return args;
    }
}
